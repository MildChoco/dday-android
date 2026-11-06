package com.simple.dday.data

/** 후원 받는 곳. 주소를 비워 두면 설정 화면에 그 줄이 나오지 않는다 */
object Support {
    /** 카카오페이 송금 링크 (https://qr.kakaopay.com/…) */
    const val KAKAOPAY = "https://qr.kakaopay.com/Ej7szdxVo"

    /** GitHub Sponsors 페이지 (https://github.com/sponsors/…) */
    const val GITHUB_SPONSORS = ""

    /** 설정 화면에 보여줄 (이름, 주소) 목록. 주소가 있는 것만 */
    val links: List<Pair<String, String>>
        get() = listOf(
            "카카오페이로 보내기" to KAKAOPAY,
            "GitHub Sponsors로 후원하기" to GITHUB_SPONSORS,
        ).filter { it.second.isNotBlank() }
}
