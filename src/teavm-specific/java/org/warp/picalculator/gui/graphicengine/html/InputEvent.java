package org.warp.picalculator.gui.graphicengine.html;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

public interface InputEvent extends Event {

    @JSProperty
    String getValue();
}
