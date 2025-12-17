package singleplayer.card;

import foundation.input.ButtonRegister;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import render.*;
import render.level.tile.RenderElement;
import render.types.box.UIDisplayBox;
import render.types.box.UIDisplayBoxButtonHandler;
import render.types.container.UIContainer;
import render.types.text.AbstractUITooltip;

import java.awt.*;
import java.io.*;

public class Card implements Serializable, Writable {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final float WIDTH = 12, HEIGHT = 14;
    public static UIColourTheme BOX = UIColourTheme.useSameColourForAllStates(
            new Color(151, 151, 151), new Color(32, 32, 32)
    );

    public final CardType type;
    public final CardAttribute[] attributes;

    public Card(CardType type, CardAttribute... attributes) {
        this.type = type;
        this.attributes = attributes;
    }

    public UIDisplayBox createRenderer() {
        return createRenderer(emptyBox(), null);
    }

    private UIDisplayBox createRenderer(UIDisplayBox displayBox, UIDisplayBoxButtonHandler buttonHandler) {
        displayBox.setColumnVerticalAlign(0, VerticalAlign.TOP);
        displayBox.addText(1, HorizontalAlign.CENTER, type.getName());
        displayBox.addSpace(0.4f, 0);
        for (CardAttribute attribute : attributes) {
            attribute.addRenderer(displayBox);
            if (buttonHandler != null)
                buttonHandler.addTooltip(displayBox.getLastIndex(0), 0, false)
                        .add(10, AbstractUITooltip.dark(), attribute.getToolTip());
            displayBox.addSpace(0.5f, 0);
        }
        return displayBox;
    }

    private static UIDisplayBox emptyBox() {
        return new UIDisplayBox(0, 0, WIDTH, HEIGHT,
                box -> box.setColourTheme(BOX).setCorner(0.8f), false);
    }

    public UIContainer createRenderElement(GameRenderer r, ButtonRegister b, RenderOrder order, float x, float y) {
        return new UIContainer(r, b, order, x, y)
                .addRenderables((r2, b2) -> {
                    UIDisplayBox displayBox = emptyBox();
                    UIDisplayBoxButtonHandler buttonHandler = new UIDisplayBoxButtonHandler(r2, b2, order, displayBox);
                    new RenderElement(r2, order, createRenderer(displayBox, buttonHandler));
                });
    }

    public Card(DataInputStream reader) throws IOException {
        type = PacketReceiver.readEnum(CardType.class, reader);
        int length = reader.readInt();
        attributes = new CardAttribute[length];
        for (int i = 0; i < length; i++) {
            attributes[i] = PacketReceiver.readEnum(CardAttribute.class, reader);
        }
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeEnum(type, w);
        w.writeInt(attributes.length);
        for (CardAttribute attribute : attributes) {
            PacketWriter.writeEnum(attribute, w);
        }
    }
}
