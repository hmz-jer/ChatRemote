
#!/bin/bash

# Fonction pour compter les messages d'un certain type et état pour ico
count_messages_ico() {
  local message_type=$1
  local status=$2
  
  local count=$(/chemin/vers/ico/manage.sh $message_type $status)
  echo "ico - $message_type $status: $count"
}

# Fonction pour compter les messages d'un certain type et état pour icon
count_messages_icon() {
  local message_type=$1
  local status=$2
  
  local count=$(/chemin/vers/icon/manage.sh $message_type $status)
  echo "icon - $message_type $status: $count"
}

# Fonction pour compter les messages pour tous les types et états pour ico
count_all_messages_ico() {
  local message_types=("swip" "IPDSX" "IP")
  local statuses=("success" "error" "total")
  
  for message_type in "${message_types[@]}"; do
    for status in "${statuses[@]}"; do
      count_messages_ico $message_type $status
    done
  done
}

# Fonction pour compter les messages pour tous les types et états pour icon
count_all_messages_icon() {
  local message_types=("success" "waiting" "error")
  local statuses=("success" "error" "total")
  
  for message_type in "${message_types[@]}"; do
    for status in "${statuses[@]}"; do
      count_messages_icon $message_type $status
    done
  done
}

# Comptage des messages pour ICO
echo "Comptage des messages pour ICO:"
count_all_messages_ico

# Comptage des messages pour icon
echo "Comptage des messages pour icon:"
count_all_messages_icon

echo "Comptage complet terminé."
