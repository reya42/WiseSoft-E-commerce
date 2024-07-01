import React, { useEffect, useState } from 'react'
import BrandDetailChunk from './BrandDetailChunk';
import BrandProductChunk from './BrandProductsChunk';

import "./css/brandshome.css"

    const BrandsHome = () => {
    const [brandsLoading,setBrandsLoading] = useState(true);
    const [brands, setBrands] = useState([]);
    const [brandsProducts, setBrandsProducts] = useState([]);
    const [brandsProductsCurPage, setBrandsProductsCurPage] = useState([]);

    const [isThereAnyProductToMap,setIsThereAnyProductToMap]=useState(false);

    // Fetch Data
    const fetchBrands = async () => {
        try {
            const response = await fetch('http://localhost:8080/brands');
            if (!response.ok) {
            throw new Error('Network response was not ok');
            }
            const data = await response.json();
            if (data.message != null) {
            throw new Error(data.message);
            } else {
            setBrands(data);
            return data; // Markaları döndür
            }
        } catch (error) {
            alert(error.message);
        }
    };
    
    const fetchProductsForBrand = async (brandLink,page) => {
        try {
            const response = await fetch(`http://localhost:8080/brands/${brandLink}/home?page=${page}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const productsData = await response.json();
            if (productsData.message != null) {
                throw new Error(productsData.message);
            }else {
                return productsData;
            }
        } catch (error) {
            alert(error.message);
        }
    };
    
    const fetchAllProductsForAllBrands = async () => {
        const brandsData = await fetchBrands();
        if (brandsData) {
            const productsPromises = brandsData.map(async (brand) => {
                if (brand.brandLink) {
                    const products = await fetchProductsForBrand(brand.brandLink,0); // ilk başta 0'ıncı sayfa yüklenir.
                    if (products.content.length>0) {
                        setIsThereAnyProductToMap(true);
                    }
                    return products;
                }
                return [];
            });
            Promise.all(productsPromises).then(productsArrays => {
                setBrandsProducts(productsArrays);
                setBrandsLoading(false);
            });
        }
    };
    // Use Effects
    useEffect(() => {
        fetchAllProductsForAllBrands();
    }, []);

    const pageButtons = (totalPages,brandIndex) =>{
        if (totalPages > 1) {
            var buttons = [];
            for (let index = 0; index < totalPages; index++) {
                buttons.push(
                    <div className='page-button' 
                        onClick={
                            () => handlePageChange(brandIndex, index)
                            }
                        >

                    </div>
                );
            }
            return buttons;
        }
        else return null;
    }

    const handlePageChange = async (brandIndex,newPage) => {
        const thisBrand = brands[brandIndex];
        const products = await fetchProductsForBrand(thisBrand.brandLink,newPage); // Yeni sayfa için productlar yüklenir
        if (products.content.length>0) {
                                    // brandsProducts ı update etmek için onunla aynı değerleri içeren geçici bir list oluşturulur
            const updatedProducts = [...brandsProducts];
                                    // O listedeki bu brand'ın indexi yeni products'a güncellenir
            updatedProducts[brandIndex] = products;
                                    // Son olarak brandsProducts yeni listenin değeri ile değiştirilir
            setBrandsProducts(updatedProducts);
        }
    }

    return (
        !brandsLoading && isThereAnyProductToMap && (
            brands.map((brand, brandIndex)=>(
                brandsProducts[brandIndex].content.length >0 && (
                    <div className={`brands-home ${brandIndex%2==1 ? "theme": ""}`}>
                        <div className="title">
                            {brandsProducts[brandIndex].content[0].brand.brandName} En Çok Satanlar
                        </div>
                        <div className='brand-detail'>
                            <BrandDetailChunk brand={brand}/>
                        </div>
                        <div className='brand-products'>
                            <BrandProductChunk products={brandsProducts[brandIndex].content} theme={brandIndex%2==1 ? true:false}/>
                            
                                
                            <div className="brands-home-page-change-container">
                                {
                                    pageButtons(brandsProducts[brandIndex].totalPages, brandIndex)
                                }
                            </div>
                            
                        </div>
                    </div>
                )
            ))
        )
    )
}

export default BrandsHome