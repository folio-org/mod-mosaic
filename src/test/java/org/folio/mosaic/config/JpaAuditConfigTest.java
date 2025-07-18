package org.folio.mosaic.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;

@ExtendWith(MockitoExtension.class)
class JpaAuditConfigTest {

  private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock
  private FolioExecutionContext folioExecutionContext;

  @Test
  void auditorProvider_shouldReturnUserIdFromContext() {
    when(folioExecutionContext.getUserId()).thenReturn(TEST_USER_ID);
    AuditorAware<UUID> auditorAware = new JpaAuditConfig().auditorProvider(folioExecutionContext);

    Optional<UUID> result = auditorAware.getCurrentAuditor();

    assertThat(result)
      .isPresent()
      .contains(TEST_USER_ID);
  }

  @Test
  void auditorProvider_whenUserIdIsNull_shouldReturnDefaultUserId() {
    when(folioExecutionContext.getUserId()).thenReturn(null);
    AuditorAware<UUID> auditorAware = new JpaAuditConfig().auditorProvider(folioExecutionContext);

    Optional<UUID> result = auditorAware.getCurrentAuditor();

    assertThat(result)
      .isPresent()
      .contains(DEFAULT_USER_ID);
  }
}
