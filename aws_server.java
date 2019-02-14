import com.amazonaws.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Random;

public class aws_server
{
    public static boolean terminated = false;
    public static Random generator = new Random();
    public static aws_connector aws_connector_instance;

    public static void main(String[] args)
    {
        /* Initialization */
        network_manager network_manager_instance = new network_manager();
        try{network_manager_instance.initialize_network(2033);} catch(Exception e){}
        game_data.initialize();
        new game_logic().initialize();
        aws_connector_instance = new aws_connector();

        while(!terminated)
        {
            Scanner command_scanner = new Scanner(System.in);
            String command = command_scanner.nextLine();
            command = command.toLowerCase();

            if(command.compareTo("stop") == 0)
            {
                /* Terminate program */
                network_manager_instance.close_network();
                log_generator.force_write_buffer();

                System.out.println("Program terminated");
                System.exit(0);
                return;
            }
            else if(command.compareTo("showlogbuffer") == 0)
            {
                log_generator.show_buffer();
            }
            else if(command.compareTo("netstat") == 0)
            {
                System.out.println("Total client connected : " + String.valueOf(network_manager_instance.total_client));
            }
            else if(command.compareTo("userlist") == 0)
            {
                for(user_data user : game_data.users)
                {
                    System.out.println(user.id + ":: x = " + String.valueOf(user.x) + ", y = " + String.valueOf(user.y));
                }
            }
            else if(command.compareTo("runtest") == 0)
            {
                run_unit_test(aws_connector_instance);

                System.out.println("Test finished. Program terminated");
                System.exit(0);
                return;
            }
            else
            {
                System.out.println("Wrong command");
            }
        }
    }

    private static void run_unit_test(aws_connector aws)
    {
        int total_success = 0;
        boolean success = true;
        String msg = "";

        System.out.println("\033[0;37mStart Test");

        /* Test 1 */
        System.out.println("Test 1 : Write log file to S3");

        log_generator.log_buffer[0] = "12345678890!@#$%^&*()";
        log_generator.log_buffer[1] = "asdfghjklqwertyuiop";
        log_generator.log_buffer[2] = "ZXCVBNMAQWSEDRFTGYUJIKOLP";
        log_generator.file_index = -1;
        try {
            log_generator.write_logfile(3);
        } catch (Exception e){
            msg = "Cannot write to S3";
            success = false;
        }
        if(aws.bucketName.compareTo("") == 0)
        {
            msg = "Please set bucketName on aws_connector.java:36";
            success = false;
        }
        if(success)
        {
            try
            {
                S3Object object = aws.s3.getObject(aws.bucketName, log_generator.log_prefix + "-1" + log_generator.log_suffix);
                InputStream s3stream = object.getObjectContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(s3stream));
                String[] s3buffer = new String[3];
                for (int cursor = 0; cursor < 3; ++cursor)
                {
                    String line = reader.readLine();
                    s3buffer[cursor] = line;
                }

                if (log_generator.log_buffer[0].compareTo(s3buffer[0]) == 0 && log_generator.log_buffer[1].compareTo(s3buffer[1]) == 0 && log_generator.log_buffer[2].compareTo(s3buffer[2]) == 0)
                {
                    success = true;
                }
                else
                {
                    msg = "Contents mismatch";
                    success = false;
                }
            } catch (Exception e)
            {
            	e.printStackTrace();
                msg = "Cannot read from S3";
                success = false;
            }
        }
        if(success)
        {
            try
            {
                aws.s3.deleteObject(aws.bucketName, log_generator.log_prefix + "-1" + log_generator.log_suffix);
                success = true;
                total_success++;
            }
            catch (Exception e)
            {
                msg = "Cannot delete from S3";
                success = false;
            }
        }
        show_test_result(success, msg);

        /* Test 2 */
        success = true;
        System.out.println("\033[0;37mTest 2 : Validate user");
        try
        {
            if(aws_connector_instance.validate_user("tester_bot1", "123"))
            {
                if(aws_connector_instance.validate_user("@f()FDaFCVJ#$","@#()FDIFCVJ#$"))
                {
                    success = false;
                    msg = "Success to login with unregistered id @f()FDaFCVJ#$/@#()FDIFCVJ#$";
                }
                else
                {
                    success = true;
                    total_success++;
                }
            }
            else
            {
                success = false;
                msg = "Fail to login with tester_bot1/123";
            }
        }
        catch (Exception e)
        {
            success = false;
            msg = "Unable to connect to db";
        }
        show_test_result(success, msg);

        System.out.println("\033[0;37mTest result : [" + String.valueOf(total_success) + "/2] passed");
    }

    private static void show_test_result(boolean success, String msg)
    {
        if(success)
        {
            System.out.println("\033[0;32m" + "pass");
        }
        else
        {
            System.out.println("\033[0;31m" + "fail : " + msg);
        }
        System.out.print("\033[0m");
    }
}
