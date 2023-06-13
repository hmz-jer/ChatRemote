# CSV File Concatenation, Signing, and Encryption Script

This script is designed to concatenate, sign, and encrypt .csv files. It reads all .csv files from two input directories, concatenates them separately based on the file type, and writes the output to new .csv files in an output directory. Each output file is then signed and encrypted using OpenSSL. 

## Getting Started

Before running the script, you need to have OpenSSL installed on your system. If it's not already installed, you can download it from the official OpenSSL website: https://www.openssl.org/source/. 

You will also need to create a configuration file `config.cfg` with the following format:

```bash
DOSSIER_CSV_CONSUMER="/path/to/consumer/csv/directory"
DOSSIER_CSV_PSP="/path/to/psp/csv/directory"
DOSSIER_DONE="/path/to/output/directory"
DOSSIER_ERREUR="/path/to/error/directory"
KEY_PEM="/path/to/your/private/key.pem"
CER_PEM="/path/to/your/certificate/cer.pem"
```

Replace the `/path/to/...` with your actual directory paths and certificate files.

## Generating RSA Key Pair

You can generate your RSA key pair (private key and certificate) using the following command:

```bash
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cer.pem -sha256 -days 365 -subj '/CN=test'
```

You will be prompted to create a password. Remember this password as you will need it to decrypt the files later.

## Running the Script

Once you have all the necessary files and configurations, you can run the script as follows:

```bash
bash script.sh
```

The script will process the CSV files, and you will see log messages on the console showing the progress.

## Decrypting the Files

To decrypt the signed and encrypted files, use the following commands:

```bash
openssl smime -decrypt -in file.enc -binary -inform DER -inkey key.pem -out decrypted_file.csv
```

You will need to enter the password that you created when generating the RSA key pair.

## Error Handling

The script includes basic error handling. If an error occurs while reading a file, that file will be moved to the error directory specified in the configuration file. Similarly, if an output file is empty (i.e., there were no valid input files), the script will not sign or encrypt that file.

Please make sure that all directories and files used by the script are readable and writable by the user running the script.
