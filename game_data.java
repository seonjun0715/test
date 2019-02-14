import java.util.LinkedList;

public class game_data
{
    public static LinkedList<user_data> users;

    public static void initialize()
    {
        users = new LinkedList<>();
    }

    public static void update_user(user_data data)
    {
        for (user_data user : users)
        {
            if (user.id.equals(data.id))
            {
                user.x = data.x;
                user.y = data.y;
                user.rotation = data.rotation;
                return;
            }
        }
    }

    public static void remove_user(String id)
    {
        synchronized (users)
        {
            for (user_data user : users)
            {
                if (user.id.equals(id))
                {
                    users.remove(user);
                    return;
                }
            }
        }
    }

    public static boolean user_alive(String id)
    {
        for (user_data user : users)
        {
            if (user.id.equals(id))
                return user.counter == 0;
        }
        return false;
    }
}