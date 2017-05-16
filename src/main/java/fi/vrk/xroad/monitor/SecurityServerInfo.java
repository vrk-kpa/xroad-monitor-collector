package fi.vrk.xroad.monitor;

import lombok.*;

/**
 * Data structure for holding security server information
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class SecurityServerInfo {
  private String serverCode;
  private String address;
  private String memberClass;
  private String memberCode;
  private String memberName;
}
