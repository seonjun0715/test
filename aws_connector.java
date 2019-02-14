/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 *  This file was modified by SeHoon Yang(sehoon.yang@e4net.net), under Apache2.0 License
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class aws_connector
{
    BasicAWSCredentials credentials;
    AmazonS3 s3;

    /* Referenced by the test function */
    public static String bucketName = "teste4net";

    public aws_connector()
    {
        try
        {
            credentials = new BasicAWSCredentials("AKIAI7K6L6KGIYIPJ2DQ", "br0pHQcK9Ux723DQqBwhTyzi4NflUSPClPzmKny9");
        }
        catch (Exception e)
        {
            throw new AmazonClientException("Key error", e);
        }
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);

        s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("ap-northeast-2").build();
    }

    public boolean validate_user(String id, String pw) throws Exception
    {
        	
        Class.forName("org.mariadb.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://mariadb.cimaqv2m05vn.ap-northeast-2.rds.amazonaws.com:3306/aws_server_db", "root", "whdrkssk");
        String sql = "select * from user where user_name=? and user_pwd=?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, pw);
        ResultSet rs = pstmt.executeQuery();
        
        boolean result = false;
        
        if(rs.next()) {
        	result = true;
        }
        
        rs.close();
        pstmt.close();
        connection.close();
        return result;
    }

    public void uploadLog(String filename, int count) throws Exception{
    	s3.putObject(new PutObjectRequest(bucketName, filename, createSampleFile(count)));
    }
    /*
     *  Reference function
     */
    private static File createSampleFile(int count) throws IOException
    {
        File file = File.createTempFile("aws-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        for(int i = 0; i < count; i++) {
        	writer.write(log_generator.log_buffer[i] + "\n");
        }
//        writer.write("abcdefghijklmnopqrstuvwxyz\n");
//        writer.write("01234567890112345678901234\n");
//        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
//        writer.write("01234567890112345678901234\n");
//        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        return file;
    }
}
