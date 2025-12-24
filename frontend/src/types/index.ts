export interface Task {
    id?: string;
    description?: string;
    type: 'DAILY' | 'WEEKLY' | 'ONE_TIME';
    weight: 'LOW' | 'MEDIUM' | 'HIGH';
    value?: number; // Valor monetário em R$
    status?: 'PENDING' | 'COMPLETED' | 'PENDING_APPROVAL' | 'APPROVED';
    requiresProof: boolean;
    createdAt?: string;
    dayOfWeek?: number;
    scheduledDate?: string;
    aiValidated?: boolean;
    proofImageUrl?: string;
    acknowledged?: boolean;
}

export interface CreateTaskRequest {
    description: string;
    type: 'DAILY' | 'WEEKLY' | 'ONE_TIME';
    weight: 'LOW' | 'MEDIUM' | 'HIGH';
    requiresProof: boolean;
    dayOfWeek?: number;
    scheduledDate?: string;
}

export interface User {
    id?: string;
    name: string;
    email?: string;
    role?: 'PARENT' | 'CHILD';
    parentId?: string;
    phoneNumber?: string;
    monthlyAllowance?: number;
    age?: number;
    subscriptionTier?: 'FREE' | 'PREMIUM';
    subscriptionStatus?: 'ACTIVE' | 'CANCELED' | 'PAST_DUE' | 'NONE';
    balance?: number;
    aiContext?: string;
}

export interface WithdrawalRequest {
    amount: number;
}

export interface CreateChildRequest {
    name: string;
    parentId: string;
    phoneNumber: string;
    age?: number;
    aiContext?: string;
}


export interface CreateParentRequest {
    name: string;
    email: string;
    phoneNumber: string;
    password?: string;
}

export interface GiftCard {
    id: string;
    name: string;
    brand: string;
    value: number;
    imageUrl?: string;
    description: string;
}

