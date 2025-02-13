package com.example.playsnapui.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.R
import com.example.playsnapui.data.Games

class HomeAdapterPopular(private val gameList: ArrayList<Games>) : RecyclerView.Adapter<HomeAdapterPopular.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapterPopular.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_popgame_list_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HomeAdapterPopular.MyViewHolder, position: Int) {

        val game : Games = gameList[position]
        holder.gamesName.text = game.namaPermainan

    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val gamesName : TextView = itemView.findViewById(R.id.title_game)
    }


}