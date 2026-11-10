//! JNI entry points for the Android app.

mod convert;

use jni::EnvUnowned;
use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JClass, JString};

/// Backs `com.tonekyilay.romaji.data.Romanizer.romanize`; the symbol name is the link, so rename both or neither.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_tonekyilay_romaji_data_Romanizer_romanize<'caller>(
    mut env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    korean: JString<'caller>,
) -> JString<'caller> {
    // Errors and panics become a Java RuntimeException instead of crossing the FFI boundary.
    env.with_env(|env| JString::from_str(env, convert::romanize(&korean.to_string())))
        .resolve::<ThrowRuntimeExAndDefault>()
}
