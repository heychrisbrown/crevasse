Crevasse
=

A script to back up a local directory to [Amazon Glacier](http://aws.amazon.com/glacier/).

Requirements
-

* A Java runtime; 1.6 is known to work
* An Amazon Web Services account with a Glacier vault already created.
* A properties file containing your AWS credentials, aping from [sample-aws-credentials.properties](crevasse/blob/master/src/main/resources/sample-aws-credentials.properties) if you'd like.

Running
-
Running the backup is simple:

    bin/glacier-backup --from=/Users/me/my-local-backup --to=vault_name --credentials=AwsCredentials.properties --inventoryFile=inventory.txt

The file specified by --inventoryFile will contain a mapping from local files to archives in the vault. You will want this in order to do a meaningful restore.
