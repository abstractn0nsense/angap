package com.angap.photosendguide

data class GuideStep(
    val title: String,
    val description: String,
)

val guideSteps = listOf(
    GuideStep(
        title = "메시지 앱을 여세요",
        description = "휴대전화에서 메시지 앱을 찾아 눌러 주세요.",
    ),
    GuideStep(
        title = "자녀와의 대화를 고르세요",
        description = "사진을 보낼 자녀의 이름이 있는 대화를 눌러 주세요.",
    ),
    GuideStep(
        title = "사진 버튼을 누르세요",
        description = "메시지 입력칸 근처의 사진 또는 + 버튼을 눌러 주세요.",
    ),
    GuideStep(
        title = "보낼 사진을 고르세요",
        description = "사진 목록에서 보내고 싶은 사진을 한 번 눌러 선택해 주세요.",
    ),
    GuideStep(
        title = "보내기 버튼을 누르세요",
        description = "선택한 사진을 확인한 뒤, 보내기 버튼을 눌러 주세요.",
    ),
)
