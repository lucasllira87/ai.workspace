package com.aiworkspace.iam.domain;

import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.event.UserRegisteredEvent;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.FullName;
import com.aiworkspace.iam.domain.valueobject.HashedPassword;
import com.aiworkspace.iam.domain.valueobject.Role;
import com.aiworkspace.iam.domain.valueobject.UserStatus;
import com.aiworkspace.shared.domain.DomainEvent;
import com.aiworkspace.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final Email EMAIL = new Email("john@example.com");
    private static final HashedPassword PASSWORD = HashedPassword.ofHash("$2a$12$hashed");
    private static final FullName NAME = new FullName("John Doe");

    @Test
    void create_producesActiveUserWithUserRole() {
        User user = User.create(EMAIL, PASSWORD, NAME);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getFullName()).isEqualTo(NAME);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getRoles()).contains(Role.USER);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void create_registersUserRegisteredEvent() {
        User user = User.create(EMAIL, PASSWORD, NAME);

        List<DomainEvent> events = user.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(UserRegisteredEvent.class);

        UserRegisteredEvent event = (UserRegisteredEvent) events.get(0);
        assertThat(event.userId()).isEqualTo(user.getId());
        assertThat(event.email()).isEqualTo(EMAIL);
    }

    @Test
    void create_doesNotStoreRawPassword() {
        HashedPassword hashed = HashedPassword.ofHash("$2a$12$someHash");
        User user = User.create(EMAIL, hashed, NAME);

        assertThat(user.getPassword().value()).doesNotContain("plaintext");
        assertThat(user.getPassword()).isEqualTo(hashed);
    }

    @Test
    void block_changesStatusToBlocked() {
        User user = User.create(EMAIL, PASSWORD, NAME);

        user.block();

        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void block_throwsWhenAlreadyBlocked() {
        User user = User.create(EMAIL, PASSWORD, NAME);
        user.block();

        assertThatThrownBy(user::block)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already blocked");
    }

    @Test
    void activate_restoresActiveStatus() {
        User user = User.create(EMAIL, PASSWORD, NAME);
        user.block();

        user.activate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void changePassword_updatesPasswordField() {
        User user = User.create(EMAIL, PASSWORD, NAME);
        HashedPassword newHash = HashedPassword.ofHash("$2a$12$newHash");

        user.changePassword(newHash);

        assertThat(user.getPassword()).isEqualTo(newHash);
    }

    @Test
    void changePassword_throwsWhenSameHash() {
        User user = User.create(EMAIL, PASSWORD, NAME);

        assertThatThrownBy(() -> user.changePassword(PASSWORD))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("differ");
    }

    @Test
    void clearDomainEvents_removesAllEvents() {
        User user = User.create(EMAIL, PASSWORD, NAME);
        assertThat(user.getDomainEvents()).isNotEmpty();

        user.clearDomainEvents();

        assertThat(user.getDomainEvents()).isEmpty();
    }

    @Test
    void email_normalizesToLowerCase() {
        Email email = new Email("John@EXAMPLE.COM");

        assertThat(email.value()).isEqualTo("john@example.com");
    }

    @Test
    void email_rejectsInvalidFormat() {
        assertThatThrownBy(() -> new Email("not-an-email"))
                .isInstanceOf(DomainException.class);
    }
}
