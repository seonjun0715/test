import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;

public class network_manager
{
    boolean is_closed = true;
    int total_client = 0;

    private class AcceptingThread extends Thread
    {
        ServerSocket listener;

        public AcceptingThread(ServerSocket listener)
        {
            this.listener = listener;
        }

        public void run()
        {
            while (!is_closed)
            {
                try
                {
                    Socket client = listener.accept();
                    total_client += 1;
                    log_generator.put_log("Client accepted, total client : " + String.valueOf(total_client));
                    NetworkThread client_thread = new NetworkThread(client);
                    client_thread.start();
                }
                catch (Exception e){}
            }

            try{listener.close();}catch (Exception e){}
            log_generator.put_log("Accepting thread terminated");
        }
    }

    private class NetworkThread extends Thread
    {
        Socket client;
        byte[] header_buffer = new byte[2];
        String[] id = new String[1];

        NetworkThread(Socket client)
        {
            this.client = client;
        }

        public void run()
        {
            try
            {
                log_generator.put_log("Start client thread");
                run_task();
            } catch(Exception e)
            {
                try{client.close();}catch (Exception f){}
                game_data.remove_user(id[0]);
                log_generator.put_log("Client terminated");
                total_client -= 1;
            }
        }

        private void run_task() throws Exception
        {
            while (!is_closed)
            {
                /* Read header and get payload length*/
                InputStream input_stream = client.getInputStream();
                input_stream.read(header_buffer, 0, 2);

                byte packet_type = header_buffer[0];
                int packet_length = header_buffer[1] * 4;

                /* Read payload */
                int total_read = 0;
                byte[] payload_buffer = new byte[packet_length];
                while(total_read < packet_length)
                {
                    total_read += input_stream.read(payload_buffer, total_read, packet_length - total_read);
                }

                /* Generate payload */
                byte[] output_buffer = payload_manager.generate_payload(packet_type, payload_buffer, id);

                /* Send packet */
                OutputStream output_stream = client.getOutputStream();
                output_stream.write(output_buffer, 0, output_buffer.length);

                /* Sleep for 30 ms */
                Thread.sleep(30);
            }

            client.close();
            game_data.remove_user(id[0]);
            log_generator.put_log("Client terminated");
        }
    }

    public void initialize_network(int port_num) throws Exception
    {
        this.is_closed = false;

        /* Start accepting loop */
        log_generator.put_log("Start accepting loop");
        AcceptingThread accepting_thread = new AcceptingThread(new ServerSocket(port_num));
        accepting_thread.start();
    }

    public void close_network()
    {
        this.is_closed = true;
    }
}