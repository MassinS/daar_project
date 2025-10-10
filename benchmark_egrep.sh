#!/bin/bash
FILE="Samples/56667-0.txt"
PATTERNS_FILE="Samples/patterns.txt"

echo "pattern,time_ms,occurrences" > Result/egrep_results.csv

while IFS= read -r pattern; do
    if [[ -z "$pattern" ]] || [[ "$pattern" == \#* ]]; then
        continue
    fi
    
    start_time=$(date +%s%N)
    count=$(egrep -o "$pattern" "$FILE" | wc -l)
    end_time=$(date +%s%N)
    
    time_ms=$(( (end_time - start_time) / 1000000 ))
    
    echo "$pattern,$time_ms,$count" >> Result/egrep_results.csv
    echo "Pattern: $pattern - Time: ${time_ms}ms - Count: $count"
done < "$PATTERNS_FILE"