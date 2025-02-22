package com.example.playsnapui.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.R
import com.example.playsnapui.data.Games

class HomeAdapterForYou (private val gameList: ArrayList<Games>) : RecyclerView.Adapter<HomeAdapterForYou.MyViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeAdapterForYou.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_foryougame_list_item, parent, false)

        return HomeAdapterForYou.MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    override fun onBindViewHolder(holder: HomeAdapterForYou.MyViewHolder, position: Int) {
        val game : Games = gameList[position]
        holder.gamesName.text = game.namaPermainan
    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val gamesName : TextView = itemView.findViewById(R.id.title_game)
    }

    fun updateGames(newGames: List<Games>) {
        gameList.clear()
        gameList.addAll(newGames)
        notifyDataSetChanged()
    }

}