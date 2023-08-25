find / -type f -not -path "/dev/*" -exec du -h 2>/dev/null {} + | sort -rh | head -1
