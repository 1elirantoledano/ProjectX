package entities;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import logic.FilesConverter;

public class ProductEntity implements Serializable{
	private Integer productID;
	private String productName;
	private String productType;
	private Double productPrice;
	private String productDescription;
	private String productDominantColor;
    private byte[] productImage;
	private String Sale="";
	private Double SalePrice;
	
	/**
	 * Constructor for the ProductEntity.java class
	 * 
	 */

	public ProductEntity()
	{
		
	}
	public ProductEntity(Integer productID, String productName, String productType, Double productPrice,
			String productDescription, String productDominantColor,byte[]productImage) {
		super();
		this.productID = productID;
		this.productName = productName;
		this.productType = productType;
		this.productPrice = productPrice;
		this.productDescription = productDescription;
		this.productDominantColor = productDominantColor;
    	this.productImage = productImage;
	}
	
	
	/**
	 * Getter for the productID
	 * @return the productID
	 */
	public String getSale() {
		return Sale;
	}
	public void setSale(String sale) {
		Sale += sale;
	}
	public void setSale(int clear) {
		Sale ="";
	}
	public byte[] getProductImage() {
		return productImage;
	}

	public void setProductImage(byte[] image1) {
		
		this.productImage = image1;
	}
public void setProductImage(String filePath) throws IOException {
		
		File file = new File (filePath);
		this.productImage = FilesConverter.convertFileToByteArray(file);
}
public void setProductImage() throws IOException {
	this.productImage =null;
}

	public Integer getProductID() {
		return productID;
	}
	/**
	 * Setter for the productID
	 * @param productID the productID to set
	 */
	public void setProductID(Integer productID) {
		this.productID = productID;
	}
	/**
	 * Getter for the productName
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}
	/**
	 * Setter for the productName
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}
	/**
	 * Getter for the productType
	 * @return the productType
	 */
	public String getProductType() {
		return productType;
	}
	/**
	 * Setter for the productType
	 * @param productType the productType to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}
	/**
	 * Getter for the productPrice
	 * @return the productPrice
	 */
	public Double getProductPrice() {
		return productPrice;
	}
	/**
	 * Setter for the productPrice
	 * @param productPrice the productPrice to set
	 */
	public void setProductPrice(Double productPrice) {
		this.productPrice = productPrice;
	}
	/**
	 * Getter for the productDescription
	 * @return the productDescription
	 */
	public String getProductDescription() {
		return productDescription;
	}
	/**
	 * Setter for the productDescription
	 * @param productDescription the productDescription to set
	 */
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	/**
	 * Getter for the productDominantColor
	 * @return the productDominantColor
	 */
	public String getProductDominantColor() {
		return productDominantColor;
	}
	/**
	 * Setter for the productDominantColor
	 * @param productDominantColor the productDominantColor to set
	 */
	public void setProductDominantColor(String productDominantColor) {
		this.productDominantColor = productDominantColor;
	}
	public Double getSalePrice() {
		return SalePrice;
	}
	public void setSalePrice(Double salePrice) {
		SalePrice = salePrice;
	}

}
