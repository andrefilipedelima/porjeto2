package com.example.fatec_ipi_noite_pojeto_p1;

import java.io.Serializable;

public class ImageShare  implements Serializable {

    private String ImagePath;

    public ImageShare(String ImagePath){
        this.ImagePath = ImagePath;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

}
