// Import the RsLog class from your external jar
@Grapes(
    @Grab(group='com.yourcompany', module='your-module', version='1.0.0')
)
import com.yourcompany.yourmodule.RsLog

// Initialize your RsLog instance (assuming it has a default constructor)
def rsLog = new RsLog()

// Create your input map
def inputMap = ['key1': 'String1', 'key2': 'String2', 'key3': 'String3', 'key4': 'String4', 'key5': 'String5']

// Call the log() method
rsLog.log(inputMap['key1'], inputMap['key2'], inputMap['key3'], inputMap['key4'], inputMap['key5'])
