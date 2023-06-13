# CSV Concatenation Script

This script concatenates all CSV files in a specified directory. It has been designed to work with two types of CSV files: 'consumer-info' and 'psp-list'.

The script reads all CSV files in a directory, concatenates them while preserving the header of the first file, and outputs the concatenated content into a new file. It then signs and encrypts the file before moving it to the destination folder.

## Configuration

The script reads its configuration from a file named `config.cfg`. In this file, you must define the following variables:

- `DOSSIER_CSV_CONSUMER`: The path to the directory containing consumer CSV files.
- `DOSSIER_CSV_PSP`: The path to the directory containing PSP CSV files.
- `DOSSIER_DONE`: The path to the directory where the processed files will be moved to.
- `DOSSIER_ERREUR`: The path to the directory where the files with errors will be moved to.
- `KEY_PEM`: The path to your PEM private key.
- `CER_PEM`: The path to your PEM public key.

## Usage

Run the script by typing `./csv_concatenation.sh` in your terminal.

## Error Handling

If there is an error with a file (for example, if it does not exist, is not readable, or does not have the same number of columns), the script will move this file to the error directory defined in the configuration file. The script also checks if the directories specified in the configuration file exist and are readable, and will terminate if this is not the case.
