import java.nio.ByteBuffer;

public class payload_manager
{
    /*
        Packet type reference

        0 : Do nothing (Unused)
        1 : Login related task
            read on a server-side (send on a client-side) :
                id(16B) + pwd(16B) = 32B
            read on a client-side (send on a server-side) :
                flag(1B) + null(3B) + initial_x(4B) + initial_y(4B) = 12B
                    flag -  0 for error
                            1 for success
        2 : User location update task
            read on a server-side (send on a client-side):
                x(4B) + y(4B) + rot(8B) = 16B
            read on a client-side (send on a server-side) :
                {id(16B) + x(4B) + y(4B) + rot(8B)} * n = 32 * n B
        ** So, maximum # of player is : n * 32 / 4 = 256; n = 32 **
        3 : Same as 2; with client killed flag
     */

    public static byte[] generate_payload(byte packet_type, byte[] payload, String[] id)
    {
        byte[] output_buffer = null;
        int packet_length;

        if(packet_type == 0)
        {
            packet_length = 0 / 4;
            output_buffer = new byte[packet_length * 4 + 2];

            /* Set header */
            output_buffer[0] = 0;
            output_buffer[1] = (byte)(packet_length);
        }
        else if(packet_type == 1)
        {
            packet_length = 3;
            output_buffer = new byte[packet_length * 4 + 2];

            /* Set header */
            output_buffer[0] = 1;
            output_buffer[1] = (byte)(packet_length);

            byte[] id_buffer = new byte[16];
            byte[] pw_buffer = new byte[16];
            System.arraycopy(payload, 0, id_buffer, 0, 16);
            System.arraycopy(payload, 16, pw_buffer, 0, 16);
            String user_id = new String(id_buffer).trim();
            String user_pw = new String(pw_buffer).trim();

            log_generator.put_log("Client tried to login : " + user_id + "/" + user_pw);

            /* Not yet implemented */
            byte certificated = 0;
            try
            {
                if (aws_server.aws_connector_instance.validate_user(user_id, user_pw))
                    certificated = 1;
            } catch (Exception e)
            {
                certificated = 1;
            }

            /* Check double login */
            boolean[] found = {false};
            game_data.users.forEach(user -> found[0] |= user.id.equals(user_id));
            if(found[0])
                certificated = 0;

            output_buffer[2] = certificated;

            /* Add user to the user list */
            if(certificated == 1)
            {
                log_generator.put_log("Certificated : " + user_id + "/" + user_pw);

                int initial_x = aws_server.generator.nextInt(1000) + 100;
                int initial_y = aws_server.generator.nextInt(700) + 100;

                game_data.users.add(new user_data(user_id, initial_x, initial_y, 0));
                id[0] = user_id;

                System.arraycopy(ByteBuffer.allocate(4).putInt(initial_x).array(), 0, output_buffer, 6, 4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(initial_y).array(), 0, output_buffer, 10, 4);
            }
        }
        else if(packet_type == 2)
        {
            packet_length = (game_data.users.size() - 1) * 8;
            output_buffer = new byte[packet_length * 4 + 2];

            /* Set header */
            if(game_data.user_alive(id[0]))
            {
                /* Update user position */
                ByteBuffer wrap = ByteBuffer.wrap(payload);
                int x = wrap.getInt(0);
                int y = wrap.getInt(4);
                double rot = wrap.getDouble(8);
                game_data.update_user(new user_data(id[0], x, y, rot));
                output_buffer[0] = 2;
            }
            else
            {
                output_buffer[0] = 3;
            }
            output_buffer[1] = (byte)(packet_length);

            int i = 0;
            synchronized (game_data.users)
            {
                for (user_data user : game_data.users)
                {
                    if (user.id.equals(id[0]))
                        continue;

                    byte[] id_buffer = new byte[16];
                    byte[] id_byte = user.id.getBytes();
                    System.arraycopy(id_byte, 0, id_buffer, 0, id_byte.length);

                    System.arraycopy(id_buffer, 0, output_buffer, 2 + 0 + i * 32, 16);
                    System.arraycopy(ByteBuffer.allocate(4).putInt(user.x).array(), 0, output_buffer, 2 + 16 + i * 32, 4);
                    System.arraycopy(ByteBuffer.allocate(4).putInt(user.y).array(), 0, output_buffer, 2 + 20 + i * 32, 4);
                    System.arraycopy(ByteBuffer.allocate(8).putDouble(user.rotation).array(), 0, output_buffer, 2 + 24 + i * 32, 8);
                    i++;
                }
            }
        }

        return output_buffer;
    }
}
