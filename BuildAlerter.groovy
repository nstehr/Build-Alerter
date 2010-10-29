import com.phidgets.InterfaceKitPhidget
import com.phidgets.event.*

public class Alerter{
	
	def interfaceKit
	def feedUrl
	def nightlyUrl
	
	def Alerter(){
		
		def listener = [
		                attached:{ae -> println "Phidget detected"}
		                ] as AttachListener
				
		
		interfaceKit = new InterfaceKitPhidget()
		interfaceKit.addAttachListener(listener)
		interfaceKit.openAny()
		println "waiting for attachment"
		interfaceKit.waitForAttachment()
	}
	def monitor(){
		//uses XmlSlurper, really groovy (pun intended) 
		//way to parse XML
		def feed = new XmlSlurper().parse(feedUrl)
		def nightlyFeed = new XmlSlurper().parse(nightlyUrl)
		
		//get the first (i.e most recent) entry
		def entry=feed.channel.item[0]
		def nightlyEntry = nightlyFeed.channel.item[0]
	        //compare dates
		def nightlyDate = new Date(nightlyEntry.pubDate.text())
		def incDate = new Date(entry.pubDate.text())
		//if the nightly is newer, use that
		if(nightlyDate.getTime() > incDate.getTime()){
		    entry = nightlyEntry
		    print "Using nightly build: "
		    }
		else
		    print "Using incremental build: "
		
		if(entry.description.equals("Build passed")){
			println "build successful"
			if(!interfaceKit.getOutputState(3)){
				//turn off the one switch
				interfaceKit.setOutputState(2,false)
				//turn on the other
				interfaceKit.setOutputState(3,true)
			}
		}
		else if(entry.description.equals("Build FAILED!")){
			println "build failed"
			if(!interfaceKit.getOutputState(2)){
				//turn off the one switch
				interfaceKit.setOutputState(3,false)
				//turn the other switch on
				interfaceKit.setOutputState(2,true)
			}
		}
	 }
	
	
	}

	def waitTime = 60*3*1000
	def ikActive = true
	alerter = new Alerter()
	alerter.feedUrl = 'http://localhost/cruisecontrol/rss/release1-1-0-my-project'
	alerter.nightlyUrl = 'http://localhost/cruisecontrol/rss/release1-1-0'
	
	while(1==1){
	def c = new GregorianCalendar()
	if(c.get(Calendar.HOUR_OF_DAY) >= 7 && c.get(Calendar.HOUR_OF_DAY) <= 17){
	  alerter.monitor()
	  ikActive = true
	  }
	 else{
	 if(ikActive){
	  if(alerter.interfaceKit.getOutputState(2))
	      alerter.interfaceKit.setOutputState(2,false)
	  if(alerter.interfaceKit.getOutputState(3))
	      alerter.interfaceKit.setOutputState(3,false)
	  ikActive = false
	  }
	  println "Offline"
	 }
	 Thread.sleep(waitTime)
	}
	

