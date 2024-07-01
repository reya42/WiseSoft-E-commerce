import React, { useState, useEffect } from 'react'

import { useParams } from 'react-router-dom'

import Brand from "../components/Brand"
import Product from '../components/Product';

const BrandDetail = () => {
    const { brandLink } = useParams();
    const [thisBrand, setThisBrand] = useState();
    const [loading, setLoading] = useState(true); 
    const [error, setError] = useState();
    const [currentPage, setCurrentPage] = useState(0);
    const [products, setProducts] = useState();

    
    const handlePage = (nextPage) =>
    {
        if (!loading) {
        if (nextPage && !products.last) {
            setCurrentPage(currentPage+1);
        }
        else if (!nextPage && !products.first) 
        {
            setCurrentPage(currentPage-1);
        }
        }
    } 
    

    useEffect(() => {
        const fetchBrandAndProducts = async () => {
            try {
                const brandResponse = await fetch(`http://localhost:8080/brands/${brandLink}`);
                if (brandResponse.status === 404) {
                  setError(brandResponse.text());
                  throw new Error('Brand not found');
                }
                if (!brandResponse.ok) {
                    throw new Error('Network response was not ok for brand');
                }
                const brandData = await brandResponse.json();
                if (brandData.message) {
                    setError(brandData.message);
                }
                setThisBrand(brandData); // Brand state'ini ayarla
  
                // Brand için ürünleri çek
                const productsResponse = await fetch(`http://localhost:8080/brands/${brandLink}/products?page=${currentPage}`);
                if (productsResponse.status === 404) {
                  setError(productsResponse.text());
                    throw new Error('Brand products not found');
                }
                if (!productsResponse.ok) {
                    throw new Error('Network response was not ok for products');
                }
                const productsData = await productsResponse.json();
                if (productsData.message)
                {
                  throw new Error(productsData.message);
                }
                else
                {
                  setProducts(productsData);
                  setLoading(false);
                }
            } catch (error) {
                setError(error.message);
            }
        };
  
        if (brandLink) {
            fetchBrandAndProducts();
        }
    }, [brandLink]);

    if (loading) {
        return (
            <div className='brand-detailed-page-container'>
                <div className="brand-detailed-page-brand-container" style={{color:"red"}}>
                    Loading...
                </div>
            </div>
        )
    }

    return (
        <div className='brand-detailed-page-container'>
            <div className="brand-detailed-page-brand-container">
                <Brand brand={thisBrand} showDescription={true}/>
            </div>
            {
                products && (
                    
                    <div className={`brand-detailed-page-products-container${
                        products.content && (products.content.length > 8 ? "-col-2" : "-col-1")
                    }`}>
                    {
                        products.content && products.content.length > 0 && (

                        products.content.reduce((resultArray, item, index) => {
                                const chunkIndex = Math.floor(index / 8)

                                if (!resultArray[chunkIndex]) {
                                    resultArray[chunkIndex] = []
                                }

                                resultArray[chunkIndex].push(
                                    <Product product={item} theme={true} width={'10.43%'} height={'100%'}/>
                                )

                                return resultArray
                        }, []).map((brandsDataChunk, index) => (
                                <div className='products-chunk' key={`brand-chunk-${index}`}>
                                    {brandsDataChunk}
                                </div>
                            )
                            )
                        )
                    }
                    {
                        products.totalPages > 1 && 
                        (
                            <div className='brand-detailed-changer-container'>
                                <div className={`brand-detailed-change-page${products.first?"":" active"}`} onClick={() => handlePage(false)}>
                                    {"<"}
                                </div>
                                <div className="brand-detailed-page-counter">
                                    {currentPage}
                                </div>
                                <div className={`brand-detailed-change-page${products.last?"":" active"}`} onClick={() => handlePage(true)}>
                                    {">"}
                                </div>
                            </div>
                        )
                    }
                </div>
                )
            }
        </div>
    )
}

export default BrandDetail