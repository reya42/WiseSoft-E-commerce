import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom';

import "./css/singleProduct.css"
import Questions from '../components/Questions';

const SingleProduct = () => {
    const [cContent, setCContent] = useState("desc");
    const { productLink } = useParams()
    const [product,setProduct] = useState();
    const [loading,setLoading] = useState(true);

    const [error,setError] = useState(null);

    const [currentPhoto, setCurrentPhoto] = useState(0);
    
    useEffect(()=>{
        const fetchProduct = async () => {
            try {
                const response = await fetch(`http://localhost:8080/products/${productLink}`);
                const data = await response.json();

                if (!response.ok) {
                    if (data.error != null) {
                        setError(data.error);
                        throw new Error(data.error);
                    }
                    else if (data.message != null) {
                        setError(data.message);
                        throw new Error(data.message);
                    }
                }
                else
                {
                    setProduct(data);
                    setLoading(false);
                    return data;
                }
            } catch (error) {
                console.error(error.message);
            }
        };
        fetchProduct()
    },[])


    const renderStars = () => {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <span key={i} className={i <= product.averageRating ? 'star filled' : 'star'}>
                    &#9733;
                </span>
            );
        }
        return stars;
    };

    if (error !== null)
        return (
            <div className='single_product_error'>
                {error}
            </div>
        )

    else if (loading) return (
        <div className='single_product_error'>
            Loading
        </div>
    )

    return (
        !product.productActive?
            <div className='single_product_error'>
                Böyle Bir Ürün Bulunamadı
            </div>
        :
            <div className='single_product_container'>
                {
                    JSON.parse(localStorage.getItem("user")) && (
                        JSON.parse(localStorage.getItem("user")).admin &&
                        <Link className='edit_product' to={`/admin/products/edit/${productLink}`}>
                            Ürünü Düzenle
                        </Link>
                    )
                }
                <div className="single_product_upper_part">
                    <div className="single_photos_container">
                        {
                            currentPhoto !== 0 && product.images.length > 1 &&
                            (
                                <div className="single_photos_left" onClick={() => setCurrentPhoto(currentPhoto-1)}>
                                    {"<"}
                                </div>
                            )
                        }
                        {
                            product.images[0] != null ?
                                <img src={`http://localhost:8080/images/${product.images[currentPhoto].imageId}`} alt="" className='single_photo' />
                            :
                                <div className="single_image_holder">
                                    Bu ürün'ün görseli bulunmamaktadır
                                </div>
                        }
                        {
                            currentPhoto < product.images.length-1 && product.images.length > 1  &&
                            (
                                <div className="single_photos_right" onClick={() => setCurrentPhoto(currentPhoto+1)}>
                                    {">"}
                                </div>
                            )
                        }
                        {
                            product.heroProduct && (
                                <div className="single_top_seller">
                                    En Çok Satanlar
                                </div>
                            )
                        }
                        {
                            product.productDiscountedPrice != -1 && (
                                <div className="single_is_discounted">
                                    İndirimde
                                </div>
                            )
                        }
                    </div>
                    <div className="single_product_upper_right_container">
                        <div className="single_name">
                            {product.productName}
                        </div>
                        {product.brand &&
                            <div className="single_brand">
                                Marka: {product.brand.brandName}
                            </div>
                        }
                        <div className="single_comments_container">
                            <Link to={`/products/${product.productLink}/comments`} className='single_product_stars'>
                                {renderStars()}
                                <div className="single_avarage_rating">
                                {
                                        product.averageRating % 1 == 0 ?
                                            `${product.averageRating}.0`
                                            :
                                            product.averageRating
                                }
                                </div>
                            </Link>
                            <div className="single_number_of_comments">
                                {
                                    `${product.numberOfRates} kişi yorumladı`
                                }
                            </div>
                        </div>
                        {
                            !product.thereShippingFee && (
                                <div className="single_shipping_free">
                                    Bu Ürün İçin Kargo Ücretsizdir
                                </div>
                            )
                        }
                        <div className="single_money_transactions">
                            <div className='single_price_container'>
                                <div className={`${product.productDiscountedPrice !== -1 ? "single_price_discounted" : "single_price"}`}>
                                    {product.productSellPrice} TL
                                </div>
                                {
                                    product.productDiscountedPrice !== -1 &&
                                        (
                                            <div className="single_price_discounted_price">
                                                {product.productDiscountedPrice} TL
                                            </div>
                                        )
                                }
                            </div>
                        </div>
                        {
                            product.productStock > 0 ?
                            <div className="single_add_to_cart">
                                Sepete Ekle
                                
                            </div>
                            :
                            <div className="single_no_stocks">
                                Bu Ürün'ün Stokları Tükenmiştir
                            </div>
                        }
                    </div>
                </div>
                <Questions productLink={productLink} />
                <div className="single_product_lower_part">
                    <div className="single_product_content_changer_container">
                        <div className={`single_product_content_changer${cContent == "desc" ? " active" : ""}`} onClick={() => setCContent("desc")}>
                            Ürün Açıklaması
                        </div>
                        {
                            product.productSpecs  != "" &&
                            (
                                <div className={`single_product_content_changer${cContent == "spec" ? " active" : ""}`} onClick={() => setCContent("spec")}>
                                    Ürün Özellikleri
                                </div>
                            )
                        }
                    </div>
                    <div className='single_product_content_container'>
                        {
                            cContent === "desc" ? 
                                <div dangerouslySetInnerHTML={{ __html: product.productDescription }} className="single_product_content"/>
                            :
                                <div dangerouslySetInnerHTML={{ __html: product.productSpecs }} className="single_product_content"/>
                        }
                        {
                            product.whatsInTheBox != "" &&
                            (
                                <div className="single_product_in_the_box">
                                    <div className="single_product_in_the_box_title">
                                        Kutuda Neler Var
                                    </div>
                                    <div dangerouslySetInnerHTML={{ __html: product.productWhatsInTheBox }} className='single_product_in_the_box_content'/>
                                </div>
                            )
                        }
                    </div>
                </div>
            </div>
    )
}

export default SingleProduct