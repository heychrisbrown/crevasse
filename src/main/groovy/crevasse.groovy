final projectName = 'crevasse'
final endpoint = "https://glacier.us-east-1.amazonaws.com/"

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager
import com.amazonaws.services.glacier.transfer.UploadResult
import org.slf4j.*
import org.apache.commons.cli.ParseException
import com.amazonaws.AmazonServiceException


ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(projectName)

def cli = new CliBuilder(
        usage: "${projectName} [options] --from [path] --to [vault] --credentials [file with your AWS credentials]",
        header: 'Options:')
cli.with {
    h(longOpt: 'help', "this help")
    d(longOpt: 'debug', "enable debug logging")
    from(longOpt: 'from', args:1, argName:'path', 'the path to copy from', required:true)
    to(longOpt: 'to', args:1, argName:'vault', "the already-existing vault to copy to", required:true)
    dryrun(longOpt: 'dryrun', "don't actually upload")
    credentials(longOpt: 'credentials', args:1, argName:'credentials', "file containing AWS credentials", required:true)
    inventoryFile(longOpt: 'inventoryFile', args:1, argName:'inventoryFile', "file to write, mapping from local file to archive name")
}
def options = cli.parse(args)
if (!options) System.exit(0)

if (options.h) {
    cli.usage()
    System.exit(0)
}

logger.setLevel(options.d ? ch.qos.logback.classic.Level.DEBUG : ch.qos.logback.classic.Level.INFO)

AWSCredentials credentials = new PropertiesCredentials(new File(options.credentials))
client = new com.amazonaws.services.glacier.AmazonGlacierClient(credentials)
client.setEndpoint(endpoint)
ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials)
logger.info("copying from directory=${options.from} to vault=${credentials.AWSAccessKeyId}.${options.to}")

def inventoryFile
if (options.inventoryFile) {
    inventoryFile = new File(options.inventoryFile)
    inventoryFile.createNewFile()
}

dir = new File(options.from)
if (!(dir.isDirectory())) {
    logger.fatal "not a directory, directory=${dir.canonicalPath}"
    System.exit(1)
}

dir.traverse { fileToUpload ->
    logger.debug("Processing ${fileToUpload}")
    final description = fileToUpload
    if (!options.dryrun) {
        if (canUpload(fileToUpload)) {
            try {
                UploadResult result = atm.upload(options.to, description.name, fileToUpload)
            } catch (AmazonServiceException ase) {
                if (ase.getStatusCode() == 408) {  // angry ISP?
                    logger.info("Sleeping, will retry")
                    sleep(1000 * 60 * 5);
                    UploadResult result = atm.upload(options.to, description.name, fileToUpload)
                }
            }
            logger.info("Uploaded from path=${fileToUpload} to archive=${result.getArchiveId()} in vault=${options.to}")
            inventoryFile?.append([fileToUpload, options.to, result.getArchiveId()].join("\t") + "\n")
            fileToUpload.delete()
        }
    }
}

def canUpload(file) {
    if (file.length() == 0) return false
    if (file.isDirectory()) return false
    true
}
