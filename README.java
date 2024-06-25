#!/bin/bash
check_systemd_service() {
  local service_name=$1
  systemctl is-active --quiet $service_name
  if [ $? -eq 0 ]; then
    echo "Le service $service_name est actif."
  else
    echo "Le service $service_name n'est pas actif."
  fi
}

# Vérifier l'état de Zookeeper
check_systemd_service zookeeper

# Vérifier l'état de Kafka
check_systemd_service kafka

# Vérifier l'état de  en utilisant manage.sh
echo "Vérification de l'état de ..."
/chemin/vers/manage.sh status

# Vérifier l'état de en utilisant manage.sh
echo "Vérification de l'état de ..."
/chemin/vers/manage.sh status

# Vérifier l'état de mock 
check_mock() {
  ps aux | grep '[j]ava.*mock-.jar' > /dev/null
  if [ $? -eq 0 ]; then
    echo "Le processus mock  est en cours d'exécution."
  else
    echo "Le processus mock n'est pas en cours d'exécution."
  fi
}

check_mock

echo "Vérification complète terminée."
