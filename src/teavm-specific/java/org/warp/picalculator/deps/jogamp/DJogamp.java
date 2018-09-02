package org.warp.picalculator.deps.jogamp;

import org.warp.picalculator.ClassUtils;
import org.warp.picalculator.ClassUtils.Var;

public class DJogamp {

    //
    // Unicode: Non printable controls: [0x00 - 0x1F]
    //

    /**
     * This value, {@value}, is used to indicate that the keyCode is unknown.
     */
    public static final short VK_UNDEFINED      = (short) 0x0;

           static final short VK_FREE01         = (short) 0x01;

    /** Constant for the HOME function key. ASCII: Start Of Text. */
    public static final short VK_HOME           = (short) 0x02;

    /** Constant for the END function key. ASCII: End Of Text. */
    public static final short VK_END            = (short) 0x03;

    /** Constant for the END function key. ASCII: End Of Transmission. */
    public static final short VK_FINAL          = (short) 0x04;

    /** Constant for the PRINT function key. ASCII: Enquiry. */
    public static final short VK_PRINTSCREEN    = (short) 0x05;

           static final short VK_FREE06         = (short) 0x06;
           static final short VK_FREE07         = (short) 0x07;

    /** Constant for the BACK SPACE key "\b", matching ASCII. Printable! */
    public static final short VK_BACK_SPACE     = (short) 0x08;

    /** Constant for the HORIZ TAB key "\t", matching ASCII. Printable! */
    public static final short VK_TAB            = (short) 0x09;

           /** LINE_FEED "\n", matching ASCII, n/a on keyboard. */
           static final short VK_FREE0A         = (short) 0x0A;

    /** Constant for the PAGE DOWN function key. ASCII: Vertical Tabulation. */
    public static final short VK_PAGE_DOWN      = (short) 0x0B;

    /** Constant for the CLEAR key, i.e. FORM FEED, matching ASCII. */
    public static final short VK_CLEAR          = (short) 0x0C;

    /** Constant for the ENTER key, i.e. CARRIAGE RETURN, matching ASCII. Printable! */
    public static final short VK_ENTER          = (short) 0x0D;

           static final short VK_FREE0E         = (short) 0x0E;

    /** Constant for the CTRL function key. ASCII: shift-in. */
    public static final short VK_SHIFT          = (short) 0x0F;

    /** Constant for the PAGE UP function key. ASCII: Data Link Escape. */
    public static final short VK_PAGE_UP        = (short) 0x10;

    /** Constant for the CTRL function key. ASCII: device-ctrl-one. */
    public static final short VK_CONTROL        = (short) 0x11;

    /** Constant for the left ALT function key. ASCII: device-ctrl-two. */
    public static final short VK_ALT            = (short) 0x12;

    /** Constant for the ALT_GRAPH function key, i.e. right ALT key. ASCII: device-ctrl-three. */
    public static final short VK_ALT_GRAPH      = (short) 0x13;

    /** Constant for the CAPS LOCK function key. ASCII: device-ctrl-four. */
    public static final short VK_CAPS_LOCK      = (short) 0x14;

           static final short VK_FREE15         = (short) 0x15;

    /** Constant for the PAUSE function key. ASCII: sync-idle. */
    public static final short VK_PAUSE          = (short) 0x16;

    /** <b>scroll lock</b> key. ASCII: End Of Transmission Block. */
    public static final short VK_SCROLL_LOCK    = (short) 0x17;

    /** Constant for the CANCEL function key. ASCII: Cancel. */
    public static final short VK_CANCEL         = (short) 0x18;

           static final short VK_FREE19         = (short) 0x19;

    /** Constant for the INSERT function key. ASCII: Substitute. */
    public static final short VK_INSERT         = (short) 0x1A;

    /** Constant for the ESCAPE function key. ASCII: Escape. */
    public static final short VK_ESCAPE         = (short) 0x1B;

    /** Constant for the Convert function key, Japanese "henkan". ASCII: File Separator. */
    public static final short VK_CONVERT        = (short) 0x1C;

    /** Constant for the Don't Convert function key, Japanese "muhenkan". ASCII: Group Separator.*/
    public static final short VK_NONCONVERT     = (short) 0x1D;

    /** Constant for the Accept or Commit function key, Japanese "kakutei". ASCII: Record Separator.*/
    public static final short VK_ACCEPT         = (short) 0x1E;

    /** Constant for the Mode Change (?). ASCII: Unit Separator.*/
    public static final short VK_MODECHANGE     = (short) 0x1F;

    //
    // Unicode: Printable [0x20 - 0x7E]
    // NOTE: Collision of 'a' - 'x' [0x61 .. 0x78], used for keyCode/keySym Fn function keys
    //

    /** Constant for the SPACE function key. ASCII: SPACE. */
    public static final short VK_SPACE          = (short) 0x20;

    /** Constant for the "!" key. */
    public static final short VK_EXCLAMATION_MARK = (short) 0x21;

    /** Constant for the """ key. */
    public static final short VK_QUOTEDBL       = (short) 0x22;

    /** Constant for the "#" key. */
    public static final short VK_NUMBER_SIGN    = (short) 0x23;

    /** Constant for the "$" key. */
    public static final short VK_DOLLAR         = (short) 0x24;

    /** Constant for the "%" key. */
    public static final short VK_PERCENT        = (short) 0x25;

    /** Constant for the "&" key. */
    public static final short VK_AMPERSAND      = (short) 0x26;

    /** Constant for the "'" key. */
    public static final short VK_QUOTE          = (short) 0x27;

    /** Constant for the "(" key. */
    public static final short VK_LEFT_PARENTHESIS  = (short) 0x28;

    /** Constant for the ")" key. */
    public static final short VK_RIGHT_PARENTHESIS = (short) 0x29;

    /** Constant for the "*" key */
    public static final short VK_ASTERISK       = (short) 0x2A;

    /** Constant for the "+" key. */
    public static final short VK_PLUS           = (short) 0x2B;

    /** Constant for the comma key, "," */
    public static final short VK_COMMA          = (short) 0x2C;

    /** Constant for the minus key, "-" */
    public static final short VK_MINUS          = (short) 0x2D;

    /** Constant for the period key, "." */
    public static final short VK_PERIOD         = (short) 0x2E;

    /** Constant for the forward slash key, "/" */
    public static final short VK_SLASH          = (short) 0x2F;

    /** VK_0 thru VK_9 are the same as UTF16/ASCII '0' thru '9' [0x30 - 0x39] */
    public static final short VK_0              = (short) 0x30;
    /** See {@link #VK_0}. */
    public static final short VK_1              = (short) 0x31;
    /** See {@link #VK_0}. */
    public static final short VK_2              = (short) 0x32;
    /** See {@link #VK_0}. */
    public static final short VK_3              = (short) 0x33;
    /** See {@link #VK_0}. */
    public static final short VK_4              = (short) 0x34;
    /** See {@link #VK_0}. */
    public static final short VK_5              = (short) 0x35;
    /** See {@link #VK_0}. */
    public static final short VK_6              = (short) 0x36;
    /** See {@link #VK_0}. */
    public static final short VK_7              = (short) 0x37;
    /** See {@link #VK_0}. */
    public static final short VK_8              = (short) 0x38;
    /** See {@link #VK_0}. */
    public static final short VK_9              = (short) 0x39;

    /** Constant for the ":" key. */
    public static final short VK_COLON          = (short) 0x3A;

    /** Constant for the semicolon key, ";" */
    public static final short VK_SEMICOLON      = (short) 0x3B;

    /** Constant for the equals key, "<" */
    public static final short VK_LESS           = (short) 0x3C;

    /** Constant for the equals key, "=" */
    public static final short VK_EQUALS         = (short) 0x3D;

    /** Constant for the equals key, ">" */
    public static final short VK_GREATER        = (short) 0x3E;

    /** Constant for the equals key, "?" */
    public static final short VK_QUESTIONMARK   = (short) 0x3F;

    /** Constant for the equals key, "@" */
    public static final short VK_AT             = (short) 0x40;

    /** VK_A thru VK_Z are the same as Capital UTF16/ASCII 'A' thru 'Z' (0x41 - 0x5A) */
    public static final short VK_A              = (short) 0x41;
    /** See {@link #VK_A}. */
    public static final short VK_B              = (short) 0x42;
    /** See {@link #VK_A}. */
    public static final short VK_C              = (short) 0x43;
    /** See {@link #VK_A}. */
    public static final short VK_D              = (short) 0x44;
    /** See {@link #VK_A}. */
    public static final short VK_E              = (short) 0x45;
    /** See {@link #VK_A}. */
    public static final short VK_F              = (short) 0x46;
    /** See {@link #VK_A}. */
    public static final short VK_G              = (short) 0x47;
    /** See {@link #VK_A}. */
    public static final short VK_H              = (short) 0x48;
    /** See {@link #VK_A}. */
    public static final short VK_I              = (short) 0x49;
    /** See {@link #VK_A}. */
    public static final short VK_J              = (short) 0x4A;
    /** See {@link #VK_A}. */
    public static final short VK_K              = (short) 0x4B;
    /** See {@link #VK_A}. */
    public static final short VK_L              = (short) 0x4C;
    /** See {@link #VK_A}. */
    public static final short VK_M              = (short) 0x4D;
    /** See {@link #VK_A}. */
    public static final short VK_N              = (short) 0x4E;
    /** See {@link #VK_A}. */
    public static final short VK_O              = (short) 0x4F;
    /** See {@link #VK_A}. */
    public static final short VK_P              = (short) 0x50;
    /** See {@link #VK_A}. */
    public static final short VK_Q              = (short) 0x51;
    /** See {@link #VK_A}. */
    public static final short VK_R              = (short) 0x52;
    /** See {@link #VK_A}. */
    public static final short VK_S              = (short) 0x53;
    /** See {@link #VK_A}. */
    public static final short VK_T              = (short) 0x54;
    /** See {@link #VK_A}. */
    public static final short VK_U              = (short) 0x55;
    /** See {@link #VK_A}. */
    public static final short VK_V              = (short) 0x56;
    /** See {@link #VK_A}. */
    public static final short VK_W              = (short) 0x57;
    /** See {@link #VK_A}. */
    public static final short VK_X              = (short) 0x58;
    /** See {@link #VK_A}. */
    public static final short VK_Y              = (short) 0x59;
    /** See {@link #VK_A}. */
    public static final short VK_Z              = (short) 0x5A;

    /** Constant for the open bracket key, "[" */
    public static final short VK_OPEN_BRACKET   = (short) 0x5B;

    /**Constant for the back slash key, "\" */
    public static final short VK_BACK_SLASH     = (short) 0x5C;

    /** Constant for the close bracket key, "]" */
    public static final short VK_CLOSE_BRACKET  = (short) 0x5D;

    /** Constant for the "^" key. */
    public static final short VK_CIRCUMFLEX     = (short) 0x5E;

    /** Constant for the "_" key */
    public static final short VK_UNDERSCORE     = (short) 0x5F;

    /** Constant for the "`" key */
    public static final short VK_BACK_QUOTE     = (short) 0x60;

    /** Small UTF/ASCII 'a' thru 'z' (0x61 - 0x7a) - Not used for keyCode / keySym. */

    /**
     * Constant for the F<i>n</i> function keys.
     * <p>
     * F1..F24, i.e. F<i>n</i>, are mapped from on <code>0x60+n</code> -> <code>[0x61 .. 0x78]</code>.
     * </p>
     * <p>
     * <b>Warning:</b> The F<i>n</i> function keys <b>do collide</b> with unicode characters small 'a' thru 'x'!<br/>
     * See <a href="#unicodeCollision">Unicode Collision</a> for details.
     * </p>
     */
    public static final short VK_F1             = (short) ( 0x60+ 1 );

    /** Constant for the F2 function key. See {@link #VK_F1}. */
    public static final short VK_F2             = (short) ( 0x60+ 2 );

    /** Constant for the F3 function key. See {@link #VK_F1}. */
    public static final short VK_F3             = (short) ( 0x60+ 3 );

    /** Constant for the F4 function key. See {@link #VK_F1}. */
    public static final short VK_F4             = (short) ( 0x60+ 4 );

    /** Constant for the F5 function key. See {@link #VK_F1}. */
    public static final short VK_F5             = (short) ( 0x60+ 5 );

    /** Constant for the F6 function key. See {@link #VK_F1}. */
    public static final short VK_F6             = (short) ( 0x60+ 6 );

    /** Constant for the F7 function key. See {@link #VK_F1}. */
    public static final short VK_F7             = (short) ( 0x60+ 7 );

    /** Constant for the F8 function key. See {@link #VK_F1}. */
    public static final short VK_F8             = (short) ( 0x60+ 8 );

    /** Constant for the F9 function key. See {@link #VK_F1}. */
    public static final short VK_F9             = (short) ( 0x60+ 9 );

    /** Constant for the F11 function key. See {@link #VK_F1}. */
    public static final short VK_F10            = (short) ( 0x60+10 );

    /** Constant for the F11 function key. See {@link #VK_F1}. */
    public static final short VK_F11            = (short) ( 0x60+11 );

    /** Constant for the F12 function key. See {@link #VK_F1}.*/
    public static final short VK_F12            = (short) ( 0x60+12 );

    /** Constant for the F13 function key. See {@link #VK_F1}. */
    public static final short VK_F13            = (short) ( 0x60+13 );

    /** Constant for the F14 function key. See {@link #VK_F1}. */
    public static final short VK_F14            = (short) ( 0x60+14 );

    /** Constant for the F15 function key. See {@link #VK_F1}. */
    public static final short VK_F15            = (short) ( 0x60+15 );

    /** Constant for the F16 function key. See {@link #VK_F1}. */
    public static final short VK_F16            = (short) ( 0x60+16 );

    /** Constant for the F17 function key. See {@link #VK_F1}. */
    public static final short VK_F17            = (short) ( 0x60+17 );

    /** Constant for the F18 function key. See {@link #VK_F1}. */
    public static final short VK_F18            = (short) ( 0x60+18 );

    /** Constant for the F19 function key. See {@link #VK_F1}. */
    public static final short VK_F19            = (short) ( 0x60+19 );

    /** Constant for the F20 function key. See {@link #VK_F1}. */
    public static final short VK_F20            = (short) ( 0x60+20 );

    /** Constant for the F21 function key. See {@link #VK_F1}. */
    public static final short VK_F21            = (short) ( 0x60+21 );

    /** Constant for the F22 function key. See {@link #VK_F1}. */
    public static final short VK_F22            = (short) ( 0x60+22 );

    /** Constant for the F23 function key. See {@link #VK_F1}. */
    public static final short VK_F23            = (short) ( 0x60+23 );

    /** Constant for the F24 function key. See {@link #VK_F1}. */
    public static final short VK_F24            = (short) ( 0x60+24 );


    /** Constant for the "{" key */
    public static final short VK_LEFT_BRACE     = (short) 0x7B;
    /** Constant for the "|" key */
    public static final short VK_PIPE           = (short) 0x7C;
    /** Constant for the "}" key */
    public static final short VK_RIGHT_BRACE    = (short) 0x7D;

    /** Constant for the "~" key, matching ASCII */
    public static final short VK_TILDE          = (short) 0x7E;

    //
    // Unicode: Non printable controls: [0x7F - 0x9F]
    //
    // Numpad keys [0x7F - 0x8E] are printable
    //

    /** Numeric keypad <b>decimal separator</b> key. Non printable UTF control. */
    public static final short VK_SEPARATOR      = (short) 0x7F;

    /** Numeric keypad VK_NUMPAD0 thru VK_NUMPAD9 are mapped to UTF control (0x80 - 0x89). Non printable UTF control. */
    public static final short VK_NUMPAD0        = (short) 0x80;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD1        = (short) 0x81;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD2        = (short) 0x82;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD3        = (short) 0x83;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD4        = (short) 0x84;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD5        = (short) 0x85;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD6        = (short) 0x86;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD7        = (short) 0x87;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD8        = (short) 0x88;
    /** See {@link #VK_NUMPAD0}. */
    public static final short VK_NUMPAD9        = (short) 0x89;

    /** Numeric keypad <b>decimal separator</b> key. Non printable UTF control. */
    public static final short VK_DECIMAL        = (short) 0x8A;

    /** Numeric keypad <b>add</b> key. Non printable UTF control. */
    public static final short VK_ADD            = (short) 0x8B;

    /** Numeric keypad <b>subtract</b> key. Non printable UTF control. */
    public static final short VK_SUBTRACT       = (short) 0x8C;

    /** Numeric keypad <b>multiply</b> key. Non printable UTF control. */
    public static final short VK_MULTIPLY       = (short) 0x8D;

    /** Numeric keypad <b>divide</b> key. Non printable UTF control. */
    public static final short VK_DIVIDE         = (short) 0x8E;

    /** Constant for the DEL key, matching ASCII. Non printable UTF control. */
    public static final short VK_DELETE         = (short) 0x93;

    /** Numeric keypad <b>num lock</b> key. Non printable UTF control. */
    public static final short VK_NUM_LOCK       = (short) 0x94;

    /** Constant for the cursor- or numerical-pad <b>left</b> arrow key. Non printable UTF control. */
    public static final short VK_LEFT           = (short) 0x95;

    /** Constant for the cursor- or numerical-pad <b>up</b> arrow key. Non printable UTF control. */
    public static final short VK_UP             = (short) 0x96;

    /** Constant for the cursor- or numerical-pad <b>right</b> arrow key. Non printable UTF control. */
    public static final short VK_RIGHT          = (short) 0x97;

    /** Constant for the cursor- or numerical pad <b>down</b> arrow key. Non printable UTF control. */
    public static final short VK_DOWN           = (short) 0x98;

    /** Constant for the Context Menu key. Non printable UTF control. */
    public static final short VK_CONTEXT_MENU   = (short) 0x99;

    /**
     * Constant for the MS "Windows" function key.
     * It is used for both the left and right version of the key.
     */
    public static final short VK_WINDOWS        = (short) 0x9A;

    /** Constant for the Meta function key. */
    public static final short VK_META           = (short) 0x9B;

    /** Constant for the Help function key. */
    public static final short VK_HELP           = (short) 0x9C;

    /** Constant for the Compose function key. */
    public static final short VK_COMPOSE        = (short) 0x9D;

    /** Constant for the Begin function key. */
    public static final short VK_BEGIN          = (short) 0x9E;

    /** Constant for the Stop function key. */
    public static final short VK_STOP           = (short) 0x9F;

    //
    // Unicode: Printable [0x00A0 - 0xDFFF]
    //

    /** Constant for the inverted exclamation mark key. */
    public static final short VK_INVERTED_EXCLAMATION_MARK = (short) 0xA1;

    /** Constant for the Euro currency sign key. */
    public static final short VK_EURO_SIGN                = (short) 0x20AC;

    //
    // Unicode: Private 0xE000 - 0xF8FF (Marked Non-Printable)
    //

    /* for Sun keyboards */
    public static final short VK_CUT            = (short) 0xF879;
    public static final short VK_COPY           = (short) 0xF87A;
    public static final short VK_PASTE          = (short) 0xF87B;
    public static final short VK_UNDO           = (short) 0xF87C;
    public static final short VK_AGAIN          = (short) 0xF87D;
    public static final short VK_FIND           = (short) 0xF87E;
    public static final short VK_PROPS          = (short) 0xF87F;

    /* for input method support on Asian Keyboards */

    /**
     * Constant for the input method on/off key.
     */
    /* Japanese PC 106 keyboard: kanji. Japanese Solaris keyboard: nihongo */
    public static final short VK_INPUT_METHOD_ON_OFF = (short) 0xF890;

    /**
     * Constant for the Code Input function key.
     */
    /* Japanese PC 106 keyboard - VK_ALPHANUMERIC + ALT: kanji bangou */
    public static final short VK_CODE_INPUT = (short) 0xF891;

    /**
     * Constant for the Roman Characters function key.
     */
    /* Japanese PC 106 keyboard: roumaji */
    public static final short VK_ROMAN_CHARACTERS = (short) 0xF892;

    /**
     * Constant for the All Candidates function key.
     */
    /* Japanese PC 106 keyboard - VK_CONVERT + ALT: zenkouho */
    public static final short VK_ALL_CANDIDATES = (short) 0xF893;

    /**
     * Constant for the Previous Candidate function key.
     */
    /* Japanese PC 106 keyboard - VK_CONVERT + SHIFT: maekouho */
    public static final short VK_PREVIOUS_CANDIDATE = (short) 0xF894;

    /**
     * Constant for the Alphanumeric function key.
     */
    /* Japanese PC 106 keyboard: eisuu */
    public static final short VK_ALPHANUMERIC   = (short) 0xF895;

    /**
     * Constant for the Katakana function key.
     */
    /* Japanese PC 106 keyboard: katakana */
    public static final short VK_KATAKANA       = (short) 0xF896;

    /**
     * Constant for the Hiragana function key.
     */
    /* Japanese PC 106 keyboard: hiragana */
    public static final short VK_HIRAGANA       = (short) 0xF897;

    /**
     * Constant for the Full-Width Characters function key.
     */
    /* Japanese PC 106 keyboard: zenkaku */
    public static final short VK_FULL_WIDTH     = (short) 0xF898;

    /**
     * Constant for the Half-Width Characters function key.
     */
    /* Japanese PC 106 keyboard: hankaku */
    public static final short VK_HALF_WIDTH     = (short) 0xF89A;

    /**
     * Constant for the Japanese-Katakana function key.
     * This key switches to a Japanese input method and selects its Katakana input mode.
     */
    /* Japanese Macintosh keyboard - VK_JAPANESE_HIRAGANA + SHIFT */
    public static final short VK_JAPANESE_KATAKANA = (short) 0xF89B;

    /**
     * Constant for the Japanese-Hiragana function key.
     * This key switches to a Japanese input method and selects its Hiragana input mode.
     */
    /* Japanese Macintosh keyboard */
    public static final short VK_JAPANESE_HIRAGANA = (short) 0xF89C;

    /**
     * Constant for the Japanese-Roman function key.
     * This key switches to a Japanese input method and selects its Roman-Direct input mode.
     */
    /* Japanese Macintosh keyboard */
    public static final short VK_JAPANESE_ROMAN = (short) 0xF89D;

    /**
     * Constant for the locking Kana function key.
     * This key locks the keyboard into a Kana layout.
     */
    /* Japanese PC 106 keyboard with special Windows driver - eisuu + Control; Japanese Solaris keyboard: kana */
    public static final short VK_KANA_LOCK = (short) 0xF89F;

    /**
     * Constant for Keyboard became invisible, e.g. Android's soft keyboard Back button hit while keyboard is visible.
     */
    public static final short VK_KEYBOARD_INVISIBLE = (short) 0xF8FF;

}
