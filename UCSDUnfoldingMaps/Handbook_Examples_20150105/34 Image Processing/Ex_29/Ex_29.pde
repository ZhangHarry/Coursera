PImage arch;

void setup() {
  size(100, 100);
  noStroke();
  arch = loadImage("arch.jpg");
}

void draw() { 
  image(arch, 0, 0);
  // Constrain to not exceed the boundary of the array
  int x = constrain(mouseX, 0, 99);  
  int y = constrain(mouseY, 0, 99);  
  loadPixels();
  color c = pixels[y*width + x];          
  fill(c);
  rect(20, 20, 60, 60);
}
