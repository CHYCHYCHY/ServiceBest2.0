package example.chy.com.servicebest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class login extends AppCompatActivity {

    private EditText username;   //用户名

    private EditText passwd;     //密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=(EditText)findViewById(R.id.username);
        passwd=(EditText)findViewById(R.id.passwd);
        Button login= (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username1=username.getText().toString();
                String passwd1=passwd.getText().toString();
                if(username1.equals("chy")&&passwd1.equals("123")){
                    Intent i=new Intent();
                    i.setClass(login.this,MainActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(login.this,"你输入的用户名或密码有误，请重新输入！",
                            Toast.LENGTH_SHORT).show();
                    username.setText("");
                    passwd.setText("");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d("login","onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("login","onResume");
        username.setText("");
        passwd.setText("");
        super.onResume();

    }

    @Override
    protected void onPause() {
        Log.d("login","onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("login","onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("login","onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d("login","onRestart");
        super.onRestart();
    }
}