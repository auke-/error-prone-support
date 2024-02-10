#!/usr/bin/env bash

# Determines, for each Error Prone Support release, with which Error Prone
# releases it is (very likely to be) compatible. The result is written to
# `./_data/compatibility.yml`, which is a source for `./compatibility.md`.

# This script relies on SDKMAN! to install compatible Maven versions.
export SDKMAN_OFFLINE_MODE=false
source "${HOME}/.sdkman/bin/sdkman-init.sh"

set -e -u -o pipefail

output_file="$(dirname "${0}")/_data/compatibility.yml"

ep_versions=$(
  git ls-remote \
      --exit-code --refs --sort='-v:refname' \
      https://github.com/google/error-prone.git \
      'v*.*' \
    | grep -oP '(?<=/v)[^/]+$'
)

work_dir="$(mktemp -d)"
trap 'rm -rf -- "${work_dir=}"' INT TERM HUP EXIT
build_log="${work_dir}/build.log"

git clone -q git@github.com:PicnicSupermarket/error-prone-support.git "${work_dir}"
pushd "${work_dir}" > /dev/null

eps_versions="$(git tag --list --sort='-v:refname' 'v*.*.*')"

# Check out the source of each Error Prone Support release, and try to build
# and test it against each Error Prone release.
for eps_version in ${eps_versions}; do
  git checkout --force "${eps_version}" --

  # Make sure to build with a compatible version of Maven.
  mvn_version="$(grep -oP '(?<=<version.maven>)[^>]+(?=</version.maven>)' pom.xml)"
  (set +u && echo n | sdk install maven "${mvn_version}")
  sdk use maven "${mvn_version}"

  # Remove any Error Prone flags used by this build that may not be compatible
  # with the targeted version of Error Prone. Removal of these build flags does
  # not influence the compatibility assessment.
  sed -i -r 's,-XepAllSuggestionsAsWarnings|-Xep:\w+:\w+,,g' pom.xml

  # Using each Error Prone release, attempt to build and test the source, while
  # also applying the Maven Central-hosted Refaster rules. This determines
  # source and behavioral (in)compatibility with Error Prone APIs, while also
  # assessing whether the Refaster rules are deserialization-compatible.
  for ep_version in ${ep_versions}; do
    echo "Testing Error Prone Support ${eps_version} with Error Prone ${ep_version}..."
    mvn clean test \
        -Perror-prone \
        -Derror-prone.patch-checks=Refaster \
        -Ppatch \
        -Pself-check \
        -Dverification.skip \
        -Dversion.error-prone-orig="${ep_version}" \
      && echo "SUCCESS: { \"eps_version\": \"${eps_version}\", \"ep_version\": \"${ep_version}\" }" || true
    # Undo any changes applied by Refaster.
    git checkout -- '*.java'
  done
done | tee "${build_log}"

popd

# Regenerate the Jekyll compatibility data file by extracting the collected
# data from the build log.
cat > "${output_file}" << EOF
# An overview of Error Prone Support releases, along with compatible Error
# Prone releases. This data was generated by \`$(basename "${0}")\`.
EOF
grep -oP '(?<=SUCCESS: ).*' "${build_log}" \
  | jq -s '.' \
  | yq -r '
      {
        "releases":
          group_by(.eps_version)
            | map({
                "version": .[0].eps_version | sub("^v", ""),
                "compatible": map(.ep_version)
              })
      }
    ' \
  | tee -a "${output_file}"