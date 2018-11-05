package io.fire.core.common.io;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.objects.IoFrame;
import io.fire.core.common.io.objects.IoFrameSet;
import io.fire.core.common.io.objects.TestStringPacket;
import io.fire.core.common.packets.PingPacket;

import javax.activation.UnsupportedDataTypeException;
import java.io.IOException;

public class test {

    public static void main(String[] args) {
        Packet testPacket = new TestStringPacket("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam in ornare urna. Proin consectetur, nibh eget luctus fringilla, ante sem dictum mi, non semper mauris sem et sem. Mauris tincidunt dignissim viverra. Quisque nec ex libero. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Mauris eget augue sagittis, ornare ipsum in, convallis leo. Aenean eget purus in eros facilisis iaculis nec a metus. Integer eu magna a felis aliquam tempus. Vivamus nec magna euismod, tempus libero ac, egestas sem. Nullam molestie mi quis diam ultrices sagittis. Sed sit amet turpis efficitur, lobortis ipsum sit amet, facilisis lectus. Etiam bibendum, magna nec congue pharetra, tellus tellus viverra dolor, at vestibulum purus erat id justo. Suspendisse ut volutpat nunc.\n" +
                "\n" +
                "Donec vel elit ut turpis luctus interdum vitae in ipsum. Ut ut mi in lacus ultrices tincidunt. In porta nunc sed leo elementum cursus. Morbi id scelerisque dolor. Fusce quis ex posuere, volutpat nunc eu, convallis nulla. Quisque ipsum est, dapibus ut nunc a, ornare porta purus. Phasellus porta elementum viverra. Praesent vel turpis vehicula, accumsan turpis sit amet, viverra metus. Suspendisse mauris dolor, lobortis eu eros id, dignissim bibendum nulla. Mauris nec lacus vel mi sollicitudin fringilla. Curabitur elementum scelerisque nisi, sit amet dapibus risus aliquet id. Integer non cursus nisi, sit amet elementum mi. Interdum et malesuada fames ac ante ipsum primis in faucibus. Donec rutrum aliquet magna eleifend maximus. Maecenas venenatis dui vel neque accumsan, non vulputate magna dapibus. Aenean posuere massa in odio tempor, at aliquam libero dictum.\n" +
                "\n" +
                "Ut finibus lacus leo, in congue justo gravida sed. Nulla sit amet massa nec risus consequat ultrices non fermentum orci. Fusce ac commodo erat. Ut velit felis, vulputate id malesuada eu, faucibus a nibh. Ut tempus sollicitudin felis. Morbi sed massa at tortor hendrerit malesuada ut eget sem. In et mauris augue. Fusce consequat, ligula ac hendrerit luctus, orci erat cursus nibh, nec volutpat sem erat nec mauris. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.\n" +
                "\n" +
                "Sed varius euismod libero, sit amet molestie nibh sagittis nec. Mauris egestas est vitae tellus bibendum, ut rutrum magna ultrices. Vivamus et lorem ac nibh maximus egestas. Mauris cursus ultrices urna, et pellentesque lorem laoreet a. Phasellus vel quam sed nibh eleifend egestas. Donec nec enim velit. In maximus tortor orci, vitae convallis nulla aliquet eget. Curabitur hendrerit massa suscipit leo sagittis, at sagittis massa efficitur.\n" +
                "\n" +
                "Sed vitae enim in risus sagittis ullamcorper. Pellentesque commodo nunc sit amet neque cursus feugiat. Praesent id dui cursus, tempus augue in, molestie nunc. Ut sit amet diam elit. Vestibulum quis faucibus urna, ac sodales nisi. Cras lobortis ex sit amet elementum fermentum. Nam et accumsan sapien. Sed faucibus turpis sed vestibulum fermentum. Ut augue sapien, tristique non felis vel, rhoncus porttitor tortor. Suspendisse ac orci non arcu commodo tincidunt nec in purus. Duis mattis ipsum facilisis risus ornare, eget lobortis ligula aliquet. Duis dictum, dolor id laoreet ullamcorper, massa ipsum bibendum turpis, id semper odio massa ut nibh. Interdum et malesuada fames ac ante ipsum primis in faucibus. Aliquam nec elit eu felis aliquam sagittis. Sed leo lorem, sodales quis pretium eu, congue ac quam.");
        IoFrameSet ioFrameSet = null;
        try {
            ioFrameSet = new IoFrameSet(testPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        IoFrameSet parser = new IoFrameSet();
        for (IoFrame frame : ioFrameSet.getFrames()) {
            String testOut = "";
            for (byte testByte : frame.getBuffer().array()) {
                testOut += testByte;
            }
            System.out.println("Frame type=" + frame.getType() + " contentLength=" + frame.getBuffer().array().length + " content=" + testOut);

            try {
                parser.readInput(frame.getBuffer().array());
            } catch (UnsupportedDataTypeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("InFrame ready=" + parser.isFinished());

            if (parser.isFinished()) {
                System.out.println("Stitched packet string= " + ((TestStringPacket) parser.getPayload()).getString());
            }
        }
    }

}
