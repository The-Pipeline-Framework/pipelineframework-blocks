#!/usr/bin/env bash
set -euo pipefail

output_dir=${1:?usage: collect-candidate-files.sh OUTPUT_DIR}
[[ ! -e "$output_dir" ]] || { echo "output directory already exists: $output_dir" >&2; exit 2; }
repo_root=$(cd "$(dirname "$0")/.." && pwd)
mkdir -p "$output_dir/repository"
version=$(python3 - "$repo_root/pom.xml" <<'PY'
import sys
import xml.etree.ElementTree as ET
print(ET.parse(sys.argv[1]).getroot().findtext("{http://maven.apache.org/POM/4.0.0}version", ""))
PY
)
local_repository="$repo_root/.m2/repository/org/pipelineframework"
for artifact_dir in "$local_repository/pipeline-blocks/$version" "$local_repository/blocks"/*/"$version"; do
  [[ -d "$artifact_dir" ]] || continue
  relative="org/pipelineframework/${artifact_dir#"$local_repository/"}"
  mkdir -p "$output_dir/repository/$(dirname "$relative")"
  destination="$output_dir/repository/$relative"
  mkdir -p "$destination"
  artifact_id=$(basename "$(dirname "$artifact_dir")")
  for file in "$artifact_dir/$artifact_id-$version.pom" "$artifact_dir/$artifact_id-$version.jar"; do
    [[ -f "$file" ]] || continue
    cp "$file" "$destination/"
  done
done
