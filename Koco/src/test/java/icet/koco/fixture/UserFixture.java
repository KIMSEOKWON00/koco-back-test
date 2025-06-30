package icet.koco.fixture;

import icet.koco.user.entity.User;

public class UserFixture {

	public static User validUser() {
		return User.builder()
			.id(1L)
			.name("테스트 유저")
			.nickname("테스트 닉네임")
			.email("test@test.com")
			.statusMsg("테스트 상태 메세지")
			.profileImgUrl("테스트.jpg")
			.build();
	}

	public static User userWithId(Long id) {
		return User.builder()
			.id(id)
			.name("유저_" + id)
			.nickname("유저_" + id)
			.email("test"+id+"@test.com")
			.statusMsg("유저_" + id + "상태메세지")
			.profileImgUrl("user_" + id + ".png")
			.build();
	}

	public static User deletedUser() {
		return User.builder()
			.id(999L)
			.name("삭제된 유저")
			.nickname("삭제된 유저")
			.email("test@test.com")
			.profileImgUrl("deleted.png")
			.statusMsg("삭제된 유저 상태메세지")
			.deletedAt(java.time.LocalDateTime.now())
			.build();
	}

	public static User anotherUser() {
		return User.builder()
			.id(2L)
			.name("다른 유저")
			.nickname("다른 유저")
			.email("another@test.com")
			.profileImgUrl("another.png")
			.statusMsg("다른 유저 상태메세지")
			.build();
	}

	public static User userWithIdAndEmail(Long id, String email) {
		return User.builder()
			.id(id)
			.email(email)
			.name("테스트 유저")
			.build();
	}
}
