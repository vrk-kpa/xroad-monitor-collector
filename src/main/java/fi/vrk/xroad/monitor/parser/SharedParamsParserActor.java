package fi.vrk.xroad.monitor.parser;

import akka.actor.AbstractActor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * Actor for parsing global configuration shared-params.xml
 */
@Component
@Scope("prototype")
@Slf4j
public class SharedParamsParserActor extends AbstractActor {
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(ParseCommand.class, this::handleParseCommand)
        .build();
  }

  private void handleParseCommand(ParseCommand msg) {
    log.info("received msg: {}", msg);
    SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
    try {
      List<SecurityServerInfo> securityServerInfos = parser.parse();
      log.info("parsed results: {}", securityServerInfos.toString());
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("failed parsing", e);
    }
  }

  public static class ParseCommand {}
}
