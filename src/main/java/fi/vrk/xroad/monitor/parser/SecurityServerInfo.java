package fi.vrk.xroad.monitor.parser;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Data structure for holding security server information
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class SecurityServerInfo {
  private final String serverCode;
  private final String address;
  private final String memberClass;
  private final String memberCode;
}
