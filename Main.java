import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

// I am unsure as I fould it little difficult but (I might be wrong in this)
// wrong or right but there is kind of imbalance between
// loss and output
class RectAvgCouple {
    ArrayList<Double> avg;
    ArrayList<double[]> rect;

    RectAvgCouple(ArrayList<Double> avg, ArrayList<double[]> rect) {
        this.avg = avg;
        this.rect = rect;
    }

    public ArrayList<Double> getAvg() {
        return this.avg;
    }

    public ArrayList<double[]> getRect() {
        return this.rect;
    }
}

class Main {
    private static final double LR = 0.1; // If it’s too high,
    // the optimization might overshoot the minimum; if it’s too low, convergence
    // may be slow.

    public static double model(double[] a, double[] w) {
        double output = 0;
        for (int i = 0; i < 4; i++) {
            output += a[i] * w[i];
        }
        return output;
    }

    public static double random() {
        Random random = new Random();
        double randomValue = random.nextDouble();
        double roundedValue = Math.round(randomValue * 10.0) / 10.0;
        return roundedValue;
    }

    public static RectAvgCouple generate() {
        ArrayList<Double> avg = new ArrayList<Double>();
        ArrayList<double[]> rect = new ArrayList<>();
        // we need to create or feed random iamges in this case we r generating via
        // pixel value
        for (int i = 0; i <= 1000; i++) {
            // 2x2
            double randomImg[] = {
                    random(),
                    random(),
                    random(),
                    random()
            };
            // sum of digit in array
            double sum = 0;
            for (int j = 0; j < 4; j++) {
                sum += randomImg[j];
            }
            sum = sum / 4;
            avg.add(sum);
            rect.add(randomImg);
        }
        return new RectAvgCouple(avg, rect);
    }

    public static double[] train(ArrayList<double[]> rect, double[] hiddenLayers, ArrayList<Double> avg) {
        ArrayList<Double> outout = new ArrayList<>();
        for (double[] i : rect) {
            double w = model(i, hiddenLayers);
            outout.add(w);
        }
        double mse = meanSquareError(outout, avg);
        System.out.println("Loss function: " + mse);
        // gradient decent for optimization
        // based on d/dx = L(x) = MSE
        double[] slope = calculateGradient(outout, avg, rect);
        double[] optimized = new double[4];
        for (int i = 0; i < 4; i++) {
            // xo = x - LR dL/dx
            optimized[i] = hiddenLayers[i] - LR * slope[i];
        }
        System.out.println("Optimal solution: " + Arrays.toString(optimized));
        return optimized;
    }
    // Σ(yi − pi)^2 (1/n)

    public static double meanSquareError(ArrayList<Double> output, ArrayList<Double> average) { // L(theta)
        double sum = 0;
        for (int i = 0; i < output.size(); i++) {
            double diff = output.get(i) - average.get(i);
            sum += diff * diff;
        }
        return sum / output.size();
    }

    public static double[] calculateGradient(ArrayList<Double> output, ArrayList<Double> average,
            ArrayList<double[]> rect) {
        // ∂MSE/∂b = (2/n) * Σ(yᵢ — ŷᵢ)
        double[] gradients = new double[4];
        for (int j = 0; j < 4; j++) {
            double sum = 0;
            for (int i = 0; i < output.size(); i++) {
                double diff = output.get(i) - average.get(i);
                sum += diff * rect.get(i)[j];
            }
            gradients[j] = (2 * sum) / output.size();
        }
        return gradients;
    }

    public static void main(String[] args) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("./s1.jpg"));
            double hidden[] = {0.98, 0.04, 0.86, -0.08};
            // above random value later corrected
            // by minimizing the function
            RectAvgCouple rectCouple = generate();
            double hiddenLayers[] = train(rectCouple.getRect(), hidden, rectCouple.getAvg());
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                // 2x2
                // Luminance=0.2126×Red+0.7152×Green+0.0722×Blue
                double[] input = new double[4];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = image.getRGB(x, y);
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = (pixel) & 0xff;
                        // Calculate the luminance using the given weights
                        double luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
                        double normalizedLuminance = luminance / 255.0;
                        double l = 1.0 - normalizedLuminance;
                        input[x] = l;
                    }
                }
                double ans =  model(hiddenLayers, input) / 4;
                System.out.println(ans);
                // sn = a1w1 + a2w2 + a3w3 + ... + anwn
                // considering a 2x2 simple pixel
                // our model predicts how dark the pixel is
                // knowing the value of output to be around 0.5
                // we find what value in the weight we should put
                // my from bological neuron -> (0.5) = w1 * 1 + w2 * 1 + w3 * x + w4 * 0
                // (CHEATING)
            }
            // generate random rectangles(pixel values), generate averages of those,
            // adjust value in hidden layer and adjust

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}