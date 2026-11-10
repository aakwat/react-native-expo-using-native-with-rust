//! Hangul to the Revised Romanization of Korean.

const FIRST: char = '가';
const LAST: char = '힣';

const INITIALS: [&str; 19] = [
    "g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s", "ss", "", "j", "jj", "ch", "k", "t", "p",
    "h",
];

const VOWELS: [&str; 21] = [
    "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae", "oe", "yo", "u", "wo", "we",
    "wi", "yu", "eu", "ui", "i",
];

/// Revised Romanization spells a final by its sound, so 28 letters collapse to 7 sounds.
const FINALS: [&str; 28] = [
    "", "k", "k", "k", "n", "n", "n", "t", "l", "k", "m", "l", "l", "l", "p", "l", "m", "p", "p",
    "t", "t", "ng", "t", "t", "k", "t", "p", "t",
];

const SYLLABLES_PER_INITIAL: usize = VOWELS.len() * FINALS.len();

/// Romanizes every Hangul syllable in `text` and passes everything else through unchanged.
///
/// Each syllable is spelled on its own, so sound changes across syllables are not applied:
/// `한국어` gives `hangukeo` where Revised Romanization writes `hangugeo`.
#[must_use]
pub fn romanize(text: &str) -> String {
    let mut out = String::with_capacity(text.len());
    for c in text.chars() {
        match jamo(c) {
            Some([initial, vowel, last]) => {
                out.push_str(initial);
                out.push_str(vowel);
                out.push_str(last);
            }
            None => out.push(c),
        }
    }
    out
}

/// Splits a precomposed syllable into the romanized initial, vowel and final.
fn jamo(c: char) -> Option<[&'static str; 3]> {
    if !(FIRST..=LAST).contains(&c) {
        return None;
    }
    let index = (u32::from(c) - u32::from(FIRST)) as usize;
    Some([
        INITIALS[index / SYLLABLES_PER_INITIAL],
        VOWELS[index / FINALS.len() % VOWELS.len()],
        FINALS[index % FINALS.len()],
    ])
}

#[cfg(test)]
mod tests {
    use super::{FIRST, LAST, romanize};

    #[test]
    fn words() {
        assert_eq!(romanize("안녕하세요"), "annyeonghaseyo");
        assert_eq!(romanize("서울"), "seoul");
        assert_eq!(romanize("떡"), "tteok");
    }

    #[test]
    fn vowels_the_old_app_got_wrong_or_crashed_on() {
        assert_eq!(romanize("걔"), "gyae");
        assert_eq!(romanize("뭐"), "mwo");
        assert_eq!(romanize("외"), "oe");
    }

    #[test]
    fn finals_are_spelled_by_sound() {
        assert_eq!(romanize("밖"), "bak");
        assert_eq!(romanize("닭"), "dak");
        assert_eq!(romanize("값"), "gap");
        assert_eq!(romanize("좋"), "jot");
        assert_eq!(romanize("꽃"), "kkot");
    }

    #[test]
    fn no_assimilation_across_syllables() {
        // Revised Romanization writes gamsahamnida and tteokbokki.
        assert_eq!(romanize("감사합니다"), "gamsahapnida");
        assert_eq!(romanize("떡볶이"), "tteokboki");
    }

    #[test]
    fn everything_else_passes_through() {
        assert_eq!(romanize(""), "");
        assert_eq!(romanize("Hi 한글! 🦀"), "Hi hangeul! 🦀");
        // Past the last syllable, and a bare jamo: neither is a syllable block.
        assert_eq!(romanize("\u{D7A4}ㄱ"), "\u{D7A4}ㄱ");
    }

    #[test]
    fn every_syllable_becomes_ascii() {
        for c in FIRST..=LAST {
            let out = romanize(c.encode_utf8(&mut [0; 4]));
            assert!(!out.is_empty() && out.is_ascii(), "{c} gave {out:?}");
        }
    }
}
