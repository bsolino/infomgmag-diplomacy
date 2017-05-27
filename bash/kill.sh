for i in `lsof -i:16713 | awk '{print $2}'`; do kill $i; done
