import React from 'react'
import { addtocarticon,inspectproducticon } from '../assets'

const HeroProduct = ({product}) => {
  return (
    <div className="hero-product">
        {product.images.length>0 ?
            <img src={`http://localhost:8080/images/${product.images[0].imageId}`} alt="" />
            :
            <div className="product-img-holder">
              Bu Ürünün Görseli Bulunamadı
            </div>
        }
        <div className='productdata'>
            <div className='product_name'>
                {product.productName}
            </div>
            <div className='prices'>
                <div className={`${product.productDiscountedPrice !== -1 ? "price discounted":"discounted_price"}`} style={product.productDiscountedPrice !== -1 ? {} : {marginLeft:"0px"}}>
                    {product.productSellPrice} TL
                </div>
                {product.productDiscountedPrice !== -1 && (
                <div className='discounted_price'>
                    {product.productDiscountedPrice} TL
                </div>
                    )}
            </div>
            <div className='transactions'>
                <a href={`http://localhost:3000/products/${product.productLink}`}>
                    <img src={inspectproducticon} alt="" style={{marginRight:"5px"}}/>
                    Inspect
                </a>
                <a href='#'>
                    <img src={addtocarticon} alt="" />
                    Add To Cart
                </a>
            </div>
        </div>
    </div>
  )
}

export default HeroProduct