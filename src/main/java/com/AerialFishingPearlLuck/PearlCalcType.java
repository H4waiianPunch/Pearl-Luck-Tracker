package com.AerialFishingPearlLuck;

public enum PearlCalcType {
    WikiCalc("WikiCalc"),
    FishCaughtPearlsGained("Fish x Pearl"),
    None("Remove");

    private String label;

    PearlCalcType(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }


}