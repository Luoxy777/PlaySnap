package com.example.playsnapui.ui.recommendgame

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.example.playsnapui.ui.recommendgame.RecommendGameAdapter

class RecommendGameAdapter(private val gameList: ArrayList<Games>) : RecyclerView.Adapter<RecommendGameAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendGameAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_foryougame_list_item, parent, false)

        return RecommendGameAdapter.MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return gameList.size


    }

    override fun onBindViewHolder(holder: RecommendGameAdapter.MyViewHolder, position: Int) {
        val game : Games = gameList[position]
        holder.gamesName.text = game.namaPermainan

        Log.d("Adapter", "Game di posisi $position: ${game.namaPermainan}")
        Log.d("Adapter", "RecyclerView Height: ${holder.itemView.height}")
    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val gamesName : TextView = itemView.findViewById(R.id.title_game_foryou)
    }

}