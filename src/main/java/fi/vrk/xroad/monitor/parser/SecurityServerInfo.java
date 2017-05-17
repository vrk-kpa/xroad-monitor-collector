package fi.vrk.xroad.monitor.parser;

import lombok.*;

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
  private final String memberName;
}
