//William Dahl
//001273655
//ICSI 426 Cryptogrpahy
//April 14th, 2018

//Import the required modules
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.util.*;
import java.lang.*;
import java.awt.Color;
import java.math.BigInteger;

public class Shamir{
	public static int red, green, blue;

	public static void main(String[] args) throws IOException{
		Random rand = new Random();
		File secret = new File(args[0]);// turns the argument into a file object
		int n = Integer.parseInt(args[1]);
		int k = Integer.parseInt(args[2]);
		// creates new file objects for the encrypted and decrypted images
		String share = "share";
		File decrypted = new File("decrypted.bmp");
		// creates BufferedImages for the encrpyted and decrypted images
		BufferedImage encryptedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		BufferedImage decryptedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		//reads the input image 
		LinkedList<Integer> random_numbers = new LinkedList<Integer>();
		encryptedImage = ImageIO.read(secret);
		for(int i = 1; i < k; i++){
			int random_number = rand.nextInt(100) + 1;
			random_numbers.add(random_number);
		}

		for(int x = 1; x <= n; x++){
			for(int i = 0; i < encryptedImage.getWidth(); i++){
				for(int j = 0; j < encryptedImage.getHeight(); j++){
					Color c = new Color(encryptedImage.getRGB(i,j));// gets the integer representation of the current pixle
					red = c.getRed();
					if(red > 250)
						red = 250;
					green = c.getGreen();
					if(green > 250)
						green = 250;
					blue = c.getBlue();
					if(blue > 250)
						blue = 250;
					for(int pow = 1; pow < k; pow++){
						red += random_numbers.get(pow-1) * Math.pow(x, pow);
						green += random_numbers.get(pow-1) * Math.pow(x, pow);
						blue += random_numbers.get(pow-1) * Math.pow(x, pow);
					}
					red %= 251;
					green %= 251;
					blue %= 251;
					Color color = new Color(red, green, blue);
					encryptedImage.setRGB(i, j, color.getRGB());
				}
			}

			String shareNumber = Integer.toString(x);
			share += (shareNumber + ".bmp");
			File encrypted = new File(share);
			ImageIO.write(encryptedImage, "bmp", encrypted);
			encryptedImage = ImageIO.read(secret);
			share = "share";
		}

		Scanner scanner = new Scanner(System.in);
		LinkedList<File> shareFiles = new LinkedList<File>();
		LinkedList<Integer> shareNumbers = new LinkedList<Integer>();
		System.out.println("Enter " + k + " shares:");
		while(scanner.hasNextLine()){
			String shareName = scanner.nextLine();
			File encryptedShare = new File(shareName);
			shareFiles.add(encryptedShare);
			if(shareFiles.size() == k){
				break;
			}
		}

		System.out.println("Enter the share Numbers: ");
		while(scanner.hasNextInt()){
			int shareNumber = scanner.nextInt();
			shareNumbers.add(shareNumber);
			if(shareNumbers.size() == k){
				break;
			}
		}

		LinkedList<BufferedImage> images = new LinkedList<BufferedImage>();
		scanner.close();
		for(int i = 0; i < k; i++){
			decryptedImage = ImageIO.read(shareFiles.get(i));
			images.add(decryptedImage);
		}
		
		BigInteger mod = new BigInteger("251");	
		for(int x = 0; x < decryptedImage.getWidth(); x++){
			for(int y = 0; y < decryptedImage.getHeight(); y++){
				BigInteger foundRed = BigInteger.ZERO;
				BigInteger foundGreen = BigInteger.ZERO;
				BigInteger foundBlue = BigInteger.ZERO;
				for(int i = 0; i < k; i++){
					BigInteger num = BigInteger.ONE;
					BigInteger den = BigInteger.ONE;
					for(int j = 0; j < k; j++){
						if(j != i){
							num = num.multiply(BigInteger.valueOf(shareNumbers.get(j) * -1));
							den = den.multiply(BigInteger.valueOf(shareNumbers.get(i) - shareNumbers.get(j)));
						}
			
					}

					Color c = new Color(images.get(i).getRGB(x,y));
					red = c.getRed();
					green = c.getGreen();
					blue = c.getBlue();
					BigInteger bigRed = new BigInteger(Integer.toString(red));
					BigInteger bigGreen = new BigInteger(Integer.toString(green));
					BigInteger bigBlue = new BigInteger(Integer.toString(blue));
					BigInteger tmpRed = (bigRed.multiply(num.multiply(den.modInverse(mod)))).mod(mod);
					BigInteger tmpGreen = (bigGreen.multiply(num.multiply(den.modInverse(mod)))).mod(mod);
					BigInteger tmpBlue = (bigBlue.multiply(num.multiply(den.modInverse(mod)))).mod(mod);
					foundRed = foundRed.add(tmpRed).mod(mod);
					foundGreen = foundGreen.add(tmpGreen).mod(mod);
					foundBlue = foundBlue.add(tmpBlue).mod(mod);
				}

				Color color = new Color(foundRed.intValue(), foundGreen.intValue(), foundBlue.intValue());
				decryptedImage.setRGB(x, y, color.getRGB());
			}
		}

		ImageIO.write(decryptedImage, "bmp", decrypted);
	}
}