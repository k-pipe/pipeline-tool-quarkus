package pipelining.util.loggedtask;

import pipelining.util.html.HTMLSection;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
		//include = JsonTypeInfo.As.PROPERTY,
		property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = CompositeItem.class),
		@JsonSubTypes.Type(value = LogConsole.class),
		@JsonSubTypes.Type(value = LogMessage.class),
})
 */
public interface LogItem {

	void log(String user, HTMLSection section);

}
