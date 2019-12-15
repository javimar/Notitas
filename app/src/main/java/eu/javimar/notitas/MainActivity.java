/**
 *
 * Notitas
 *
 * @author Javier Martín
 * @email: javimardeveloper@gmail.com
 * @link http://www.javimar.eu
 * @package eu.javimar.notitas
 * @version 1.0
 *
BSD 3-Clause License

Copyright (c) 2020 JaviMar
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

 * Neither the name of the copyright holder nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package eu.javimar.notitas;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.notitas.view.FragmentNotaList;
import eu.javimar.notitas.view.NotaDetailActivity;

public class MainActivity extends AppCompatActivity implements
        FragmentNotaList.OnNotaItemSelectedListener
{
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar_main) Toolbar toolbar;

    private FragmentNotaList mFragmentNotaList;

    private static final String FRAGMENT_LIST_TAG = "fragment_list_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(view ->
        {
            // add note
            startActivity(new Intent(MainActivity.this, EditNota.class)
                .putExtra("newNota", true));
        });

        if (savedInstanceState == null)
        {
            mFragmentNotaList = new FragmentNotaList();
            mFragmentNotaList.setNotaItemListener(this);
        }
        else
        {
            mFragmentNotaList = (FragmentNotaList) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_LIST_TAG);
        }
        displayFragmentList();
    }

    private void displayFragmentList()
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(mFragmentNotaList.isAdded()) // if fragment is already in container
        {
            ft.show(mFragmentNotaList);
        }
        else
        {
            ft.add(R.id.fragment_container, mFragmentNotaList, FRAGMENT_LIST_TAG);
        }
        // commit changes
        ft.commit();
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNotaItemSelected(int id)
    {
        // Start the NotaDetailActivity and pass the id
        Intent intent = new Intent(this, NotaDetailActivity.class);
        intent.putExtra("notaId", id);
        startActivity(intent);
    }
}
