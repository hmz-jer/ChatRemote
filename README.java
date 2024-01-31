#!/bin/bash

# Générer une chaîne aléatoire de la longueur spécifiée
generate_random_string() {
    cat /dev/urandom | tr -dc 'A-Za-z0-9' | fold -w $1 | head -n 1
}

# Générer un objet JSON pour OperationsList
generate_operations_list_object() {
    cat <<EOF
    {
        "OperationId": "$(generate_random_string 16)",
        "SensitiveData": "$(generate_random_string 16)",
        "SensitiveDataType": "$(generate_random_string 16)",
        "RspnRsn": "OK",
        "RspnCode": "$(generate_random_string 4)"
    }
EOF
}

# Définir les limites initiales pour la recherche dichotomique
min_operations=1
max_operations=1000  # Ajustez ce nombre selon vos besoins

# Boucle de recherche dichotomique
while [[ $min_operations -lt $max_operations ]]; do
    operations_count=$(( (min_operations + max_operations) / 2 ))

    # Générer OperationsList
    operations_list="["
    for ((i=1; i<=operations_count; i++)); do
        operations_list+=$(generate_operations_list_object)
        if [ $i -lt $operations_count ]; then
            operations_list+=","
        fi
    done
    operations_list+="]"

    # Créer le JSON final
    json_output=$(cat <<EOF
{
    "CustMsgID": "$(generate_random_string 16)",
    "CustCnxID": "$(generate_random_string 16)",
    "RspnCode": "$(generate_random_string 4)",
    "RspnRsn": "$(generate_random_string 2)",
    "SensitiveData": "$(generate_random_string 16)",
    "SensitiveDataType": "$(generate_random_string 16)",
    "OperationsList": $operations_list
}
EOF
    )

    # Enregistrer le JSON dans un fichier
    output_file="output.json"
    echo "$json_output" > $output_file

    # Exécuter le script go-update.sh avec le certificat et le fichier JSON généré
    response=$(./go-update.sh certs/iso.cert $output_file)

    # Vérifier la réponse pour l'erreur HTTP 413
    if [[ $response == *"413"* ]]; then
        # Si erreur 413, réduire max_operations
        max_operations=$operations_count
    else
        # Sinon, augmenter min_operations
        min_operations=$((operations_count + 1))
    fi
done

echo "Le nombre maximum d'opérations avant l'erreur 413 est: $((max_operations - 1))"

# Fin du script
