import React, { useEffect, useState } from 'react'
import Brand from '../components/Brand';

import "./css/brands.css";

const Brands = () => {
    const [brands,setBrands] = useState();
    const [loading,setLoading] = useState(true);
    
    useEffect(()=>{
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
                setLoading(false);
                return data; // Markaları döndür
                }
            } catch (error) {
                alert(error.message);
            }
        };
        fetchBrands();
    },[])

    if (loading) {
        return (
            <div>
                Loading...
            </div>
        )
    }

    return (
        brands ?
        <div className="brands-container">
            {
                brands && brands.length > 0 && (
                    brands.reduce((resultArray, item, index) => {
                        const chunkIndex = Math.floor(index / 6)

                        if (!resultArray[chunkIndex]) {
                            resultArray[chunkIndex] = []
                        }

                        resultArray[chunkIndex].push(
                            <Brand brand={item}/>
                        )

                        return resultArray
                    }, []).map((brandsDataChunk, index) => (
                        <div className='brand-flex-chunk' key={`brand-flex-chunk-${index}`}>
                            {brandsDataChunk}
                        </div>
                        )
                    )
                )
            }
        </div>
        :
        <div className="brands-container no-data">
            Database'de Herhangi bir marka bulunamadı
        </div>
    )
}

export default Brands