#!/bin/bash

# This helper script creates a Tomcat-compatible, password-protected Java Keystore from
# a private key file and from a certificate file containing the server certificate and
# intermediate certificate.

KEY_FILE=$1
PEM_FILE=$2
OUT_FILE=$3

# Script starts here...
# Exit immediately if anything unexpected happens.
set -e

# Validate the supplied number of arguments
if [ $# -ne 3 ]; then
   echo ""
   echo "USAGE: jks_helper.sh <keyfile> <pem_file> <output_file>"
   echo ""
   exit 1
fi

if [ ! -f $KEY_FILE ]; then
  echo ""
  echo "ERROR: the private key file: '$KEY_FILE' does NOT exist."
  exit 1
fi

if [ ! -f $PEM_FILE ]; then
  echo ""
  echo "ERROR: the combined certificate file: '$PEM_FILE' does NOT exist."
  echo ""
  exit 1
fi

if [ -f $OUT_FILE ]; then
  echo ""
  echo "ERROR: the keystore output file: '$OUT_FILE' already exists."
  echo ""
  exit 1
fi

echo ""
echo "JKS Helper was triggered in working directory '$PWD' with arguments as follows:"
echo ""
echo "- Private Key (.key) File............... '$1'"
echo "- Combined Certicate (.pem) File........ '$2'"
echo "- Java Keystore (.jks) Output File...... '$3'"
echo ""

until [ ${#PASSWORD} -gt 5 ] ; do
  read -p "Please enter the required password (minimum 6 chars) for the new JKS keystore: " PASSWORD
  if [ ! ${#PASSWORD} -gt 5 ] ; then
     echo "ERROR: the password must be at least 6 characters in length."
     echo ""
  fi
done
  echo ""
  echo "OK: Thank you !"

echo "Creating Temporary Work Directory..."
scratch_dir=$(mktemp -d)
echo "OK. Temporary Directory created."
echo ""

# Ensure temporary directory gets cleaned up no matter how the script terminates
function finish {
  rm -rf "$scratch_dir"
}
trap finish EXIT

echo "Going into Temporary Directory: '$scratch_dir'..."
start_dir=`pwd`
pushd $scratch_dir > /dev/null
echo "OK: Done."
echo ""

echo "Extracting certificates from .PEM file: '$start_dir/$PEM_FILE'..."
# The following makes the assumption that there will be a 'BEGIN CERTIFICATE' line
# immediately before the certificate starts
split_prefix="cert_"
split -a 1 -p "BEGIN CERTIFICATE" $start_dir/$PEM_FILE $split_prefix
echo "OK: Done."
echo ""

echo "Checking Server Certificate..."
server_cert_file=$split_prefix"a"
if [ -f $server_cert_file ]; then
  echo "OK: Server Certificate exists."
  echo "OK: Done."
  echo ""
else
  echo ""
  echo "ERROR: the server certificate file was NOT extracted. (local file: '$server_cert_file' is missing)"
  echo ""
  exit 1
fi

echo "Checking Intermediate Certificate..."
intermediate_cert_file=$split_prefix"b"
if [ -f $intermediate_cert_file ]; then
  echo "OK: Intermediate Certificate exists."
  echo "OK: Done."
  echo ""
else
  echo ""
  echo "ERROR: the intermediate certificate file was NOT extracted. (local file: '$intermediate_cert_file' is missing)"
  echo ""
  exit 1
fi

echo "Checking extraction process..."
extra_file=$split_prefix"c"
if [ -f $extra_file ]; then
  echo ""
  echo "ERROR: the combined certificate (.pem) file: '$PEM_FILE' had an unexpected format (more than TWO certificates)"
  echo ""
  exit 1
else
  echo "Done: the extraction process seems to have gone as expected."
  echo ""
fi

echo "Creating temporary P12 keystore from private key and server certicate..."
openssl pkcs12 -export -inkey $start_dir/$KEY_FILE  -in cert_a -out keystore.tmp -passout pass:$PASSWORD  -name tomcat
echo "OK: Done."
echo ""

echo "Importing temporary P12ls keystore 'keystore.tmp' into JKS keystore '$start_dir/$OUT_FILE'..."
keytool -importkeystore -srckeystore keystore.tmp -srcstorepass $PASSWORD -noprompt -deststorepass $PASSWORD -keystore $start_dir/$OUT_FILE
echo "OK: Done."
echo ""

echo "Importing intermediate certificate into JKS keystore..."
keytool -importcert -file cert_b  -keypass $PASSWORD -storepass $PASSWORD -noprompt -alias ca_cert  -keystore $start_dir/$OUT_FILE
echo "OK: Done."
echo ""

echo "Displaying JKS keystore..."
keytool -list -keystore $start_dir/$OUT_FILE -storepass $PASSWORD
echo "OK: Done."
echo ""

echo "The new JKS keystore: '$start_dir/$OUT_FILE' was successfully created."
echo ""

exit 0