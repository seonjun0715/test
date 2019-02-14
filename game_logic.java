import java.util.LinkedList;

public class game_logic
{
    class CollisionCheck extends Thread
    {
        LinkedList<user_data[]> collisions = new LinkedList<>();

        public void run()
        {
            try{run_task();}catch (Exception e){e.printStackTrace();}
        }

        private void run_task() throws Exception {
            while (!aws_server.terminated)
            {
                synchronized (game_data.users)
                {
                    /* Detect Collision */
                    for (user_data attacker : game_data.users)
                    {
                        for (user_data defender : game_data.users)
                        {
                            if(attacker.id.equals(defender.id))
                                continue;

                            boolean collided = Math.pow(attacker.x + (15 + 17.32)  * Math.sin(Math.toRadians(attacker.rotation)) - defender.x, 2) + Math.pow(attacker.y - (15 + 17.32) * Math.cos(Math.toRadians(attacker.rotation)) - defender.y, 2) < 500;
                            collided = collided && defender.counter == 0;
                            if (collided)
                            {
                                log_generator.put_log("Collided : " + attacker.id + "/" + defender.id);
                                collisions.add(new user_data[]{attacker, defender});
                            }
                        }
                    }
                }

                /* Separate collided users */
                for (user_data[] colliders : collisions)
                {
                    colliders[1].x = -100000;
                    colliders[1].y = -100000;
                    colliders[1].counter = 40;
                }
                collisions.clear();

                synchronized (game_data.users)
                {
                    for (user_data user : game_data.users)
                    {
                        if (user.counter > 0)
                            user.counter--;
                    }
                }

                Thread.sleep(60);
            }
        }
    }

    void initialize()
    {
        CollisionCheck collision_check_thread = new CollisionCheck();
        collision_check_thread.start();
    }
}
